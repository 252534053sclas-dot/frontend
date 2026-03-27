import random
from datetime import date, datetime, timedelta
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .models import ChatMessage, Mood, Journal
from django.db.models import Count

class MoodAnalyticsView(APIView):
    MOOD_SCORES = {
        "😊": 5, "😇": 5, "🥰": 5,
        "😐": 3, "🤔": 3,
        "😔": 2, "😢": 1, "😡": 1, "😖": 1
    }

    def get(self, request):
        if not request.user.is_authenticated:
            return Response({"error": "Authentication required"}, status=status.HTTP_401_UNAUTHORIZED)
        
        user = request.user
        today = date.today()
        
        # --- DAY VIEW ---
        mood_today = Mood.objects.filter(user=user, timestamp__date=today).first()
        mood_yesterday = Mood.objects.filter(user=user, timestamp__date=today - timedelta(days=1)).first()
        
        day_insight = "You haven't logged your mood today yet. Start your day by sharing how you feel! 🌸"
        if mood_today:
            if mood_yesterday:
                score_today = self.MOOD_SCORES.get(mood_today.mood_type, 3)
                score_yesterday = self.MOOD_SCORES.get(mood_yesterday.mood_type, 3)
                if score_today > score_yesterday:
                    day_insight = "You seem to be feeling slightly better today! Keep that glow. ✨"
                elif score_today < score_yesterday:
                    day_insight = "Today seems a bit tough. Be gentle with yourself. 💕"
                else:
                    day_insight = "Your mood is stable compared to yesterday. Consistency is key! 🌸"
            else:
                day_insight = f"You're feeling {mood_today.mood_type} today. Thanks for checking in! ✨"

        # --- WEEK VIEW ---
        week_moods = Mood.objects.filter(user=user, timestamp__date__gte=today - timedelta(days=7))
        week_freq = week_moods.values('mood_type').annotate(count=Count('mood_type')).order_by('-count')
        most_freq_week = week_freq[0]['mood_type'] if week_freq else "No data"
        week_insight = "This week shows more calm and balanced emotions." if most_freq_week in ["😊", "😇", "😐"] else "This week has been emotionally heavy. Take some time for self-care. 🌿"

        # --- MONTH VIEW ---
        month_moods = Mood.objects.filter(user=user, timestamp__date__gte=today - timedelta(days=30))
        month_freq = month_moods.values('mood_type').annotate(count=Count('mood_type')).order_by('-count')
        most_freq_month = month_freq[0]['mood_type'] if month_freq else "No data"
        month_insight = "You've had a mostly positive month! Keep it up. ✨"
        if most_freq_month in ["😔", "😢", "😡"]:
            month_insight = "Sadness or stress appears frequently this month. Consider focusing on self-care. 🌸"

        # --- TREND ---
        score_now = sum(self.MOOD_SCORES.get(m.mood_type, 3) for m in week_moods) / len(week_moods) if week_moods else 0
        trend = "Stable"
        if score_now > 3.5: trend = "Improving"
        elif score_now < 2.5: trend = "Declining"

        return Response({
            "day": {"today": mood_today.mood_type if mood_today else None, "insight": day_insight},
            "week": {"most_frequent": most_freq_week, "insight": week_insight},
            "month": {"most_frequent": most_freq_month, "insight": month_insight},
            "trend": trend,
            "reminders": ["Take a deep breath", "Hydrate yourself", "Write in your journal"]
        }, status=status.HTTP_200_OK)

class AICompanionView(APIView):
    KNOWLEDGE_BASE = [
        {"category": "Mood Analytics", "keywords": ["analytics", "trend", "history", "stats"], "answers": ["Mood Analytics helps you see emotional patterns. 📊"]},
        {"category": "Mood Tracker", "keywords": ["mood", "feel"], "answers": ["Log your moods daily to build a history of your well-being. 😊"]},
        {"category": "AI Journal", "keywords": ["journal", "write"], "answers": ["The AI Journal is a secure space for your private thoughts. 📓"]},
        {"category": "Emergency SOS", "keywords": ["sos", "emergency"], "answers": ["In an emergency, our SOS feature sends your LIVE location. 🚨"]}
    ]

    def post(self, request):
        user_message = request.data.get('message', '').lower()
        user = request.user if request.user.is_authenticated else None
        if not user_message: return Response({"error": "Message is required"}, status=400)
        context = self.get_user_context(user)
        best_match = None
        best_score = 0
        for item in self.KNOWLEDGE_BASE:
            score = sum(1 for kw in item["keywords"] if kw in user_message)
            if score > best_score: best_score = score; best_match = item
        ai_response = self.generate_dynamic_response(user_message, best_match, context)
        if user:
            ChatMessage.objects.create(user=user, message=user_message, is_ai=False)
            ChatMessage.objects.create(user=user, message=ai_response, is_ai=True)
        return Response({"response": ai_response}, status=200)

    def get_user_context(self, user):
        ctx = {"name": "there", "trend": "Stable", "latest_mood": None}
        if not user or not user.is_authenticated: return ctx
        ctx["name"] = user.first_name or user.username
        mood = Mood.objects.filter(user=user).order_by('-timestamp').first()
        if mood: ctx["latest_mood"] = mood.mood_type
        return ctx

    def generate_dynamic_response(self, message, match, ctx):
        if any(word in message for word in ["hi", "hello", "hey"]):
            return f"Hi {ctx['name']}! I'm your Careyfem companion. How can I help you today? 🌸"
        if match: return random.choice(match["answers"])
        return f"I'm here for you, {ctx['name']}. Tell me more! ✨"

class MoodTrackerView(APIView):
    def post(self, request):
        if not request.user.is_authenticated: return Response(status=401)
        mood = request.data.get('mood_type')
        note = request.data.get('note', '')
        if not mood: return Response(status=400)
        Mood.objects.create(user=request.user, mood_type=mood, note=note)
        return Response({"message": "Mood saved"}, status=201)
    def get(self, request):
        if not request.user.is_authenticated: return Response(status=401)
        moods = Mood.objects.filter(user=request.user).order_by('-timestamp').values()
        return Response(list(moods), status=200)

class JournalView(APIView):
    def post(self, request):
        if not request.user.is_authenticated: return Response(status=401)
        content = request.data.get('content')
        if not content: return Response(status=400)
        Journal.objects.create(user=request.user, content=content, ai_insight="That sounds important!")
        return Response({"message": "Journal saved"}, status=201)
    def get(self, request):
        if not request.user.is_authenticated: return Response(status=401)
        journals = Journal.objects.filter(user=request.user).order_by('-timestamp').values()
        return Response(list(journals), status=200)
