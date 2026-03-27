from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.db import transaction
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.views import TokenObtainPairView

class SignupView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        username = request.data.get('username')
        email = request.data.get('email')
        password = request.data.get('password')

        if not username or not email or not password:
            return Response({"error": "All fields required"}, status=400)

        if User.objects.filter(username=username).exists():
            return Response({"error": "Username exists"}, status=400)

        if User.objects.filter(email=email).exists():
            return Response({"error": "Email exists"}, status=400)

        try:
            with transaction.atomic():
                user = User.objects.create_user(
                    username=username,
                    email=email,
                    password=password
                )
                
                # Generate JWT tokens
                refresh = RefreshToken.for_user(user)
                
                return Response(
                    {
                        "message": "Signup successful",
                        "refresh": str(refresh),
                        "access": str(refresh.access_token),
                        "username": user.username,
                        "email": user.email
                    },
                    status=201
                )

        except Exception as e:
            return Response(
                {"error": str(e)},
                status=500
            )

class LoginView(TokenObtainPairView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        identifier = request.data.get('username')  # email or username
        password = request.data.get('password')

        if not identifier or not password:
            return Response(
                {"error": "Credentials required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        # 🔍 Email or username login
        try:
            user = User.objects.get(email=identifier)
            username = user.username
        except User.DoesNotExist:
            username = identifier

        user = authenticate(username=username, password=password)

        if not user:
            return Response(
                {"error": "Invalid credentials"},
                status=status.HTTP_401_UNAUTHORIZED
            )

        # ✅ Generate JWT tokens
        refresh = RefreshToken.for_user(user)

        return Response(
            {
                "refresh": str(refresh),
                "access": str(refresh.access_token),
                "username": user.username,
                "email": user.email,
                "name": user.first_name
            },
            status=status.HTTP_200_OK
        )

class ResetPasswordView(APIView):
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        email = request.data.get('email')
        new_password = request.data.get('new_password')
        confirm_password = request.data.get('confirm_password')

        if not email or not new_password or not confirm_password:
            return Response(
                {"error": "All fields are required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        if new_password != confirm_password:
            return Response(
                {"error": "Passwords do not match"},
                status=status.HTTP_400_BAD_REQUEST
            )

        if len(new_password) < 6:
            return Response(
                {"error": "Password must be at least 6 characters"},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            return Response(
                {"error": "User not found"},
                status=status.HTTP_404_NOT_FOUND
            )

        user.set_password(new_password)
        user.save()

        # Invalidate old sessions if using Session auth, but for JWT we can't easily invalidate unless using blacklist app.
        # For now, just return success.
        
        return Response(
            {
                "message": "Password reset successful"
            },
            status=status.HTTP_200_OK
        )

