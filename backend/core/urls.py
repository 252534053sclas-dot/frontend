from django.urls import path
from authentication.views import LoginView
from .views import *

urlpatterns = [
    path('ai-chat/', AICompanionView.as_view(), name='ai_chat'),
    path('mood-analytics/', MoodAnalyticsView.as_view(), name='mood_analytics'),
    path('mood-tracker/', MoodTrackerView.as_view(), name='mood_tracker'),
    path('journal/', JournalView.as_view(), name='journal'),
]
