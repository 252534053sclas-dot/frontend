from django.db import models
from django.contrib.auth.models import User

class ChatMessage(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    message = models.TextField()
    is_ai = models.BooleanField(default=False)
    timestamp = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{'AI' if self.is_ai else self.user.username}: {self.message[:20]}"

class Mood(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    mood_type = models.CharField(max_length=20) # 😊, 😐, 😔, 😡
    note = models.TextField(null=True, blank=True)
    timestamp = models.DateTimeField(auto_now_add=True)

class Journal(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    content = models.TextField()
    ai_insight = models.TextField(null=True, blank=True)
    timestamp = models.DateTimeField(auto_now_add=True)

# Removed CycleInfo model as it is replaced by Mood Analytics module


