from django.contrib.auth.models import User
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer
from rest_framework.exceptions import AuthenticationFailed

class LoginSerializer(TokenObtainPairSerializer):
    def validate(self, attrs):
        identifier = attrs.get("username")
        password = attrs.get("password")

        # Try email first
        try:
            user = User.objects.get(email=identifier)
            username = user.username
        except User.DoesNotExist:
            # fallback to username
            username = identifier

        # JWT expects username internally
        attrs['username'] = username

        data = super().validate(attrs)

        # extra response fields
        data['username'] = self.user.username
        data['email'] = self.user.email

        return data
