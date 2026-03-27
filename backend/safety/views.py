from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .models import EmergencyContact, SOSAlert

class SOSView(APIView):
    def post(self, request):
        if not request.user.is_authenticated:
            return Response({"error": "Authentication required"}, status=status.HTTP_401_UNAUTHORIZED)
            
        lat = request.data.get('latitude')
        long = request.data.get('longitude')
        
        SOSAlert.objects.create(user=request.user, location_lat=lat, location_long=long)
        
        return Response({"message": "SOS sent successfully! Contacts notified."}, status=status.HTTP_201_CREATED)
    
    def get(self, request):
        if not request.user.is_authenticated:
            return Response({"error": "Authentication required"}, status=401)

        alerts = SOSAlert.objects.filter(user=request.user).order_by('-timestamp').values()
        return Response(list(alerts), status=200)

class EmergencyContactView(APIView):
    def get(self, request):
        if not request.user.is_authenticated:
            return Response({"error": "Authentication required"}, status=status.HTTP_401_UNAUTHORIZED)
        contacts = EmergencyContact.objects.filter(user=request.user).values()
        return Response(list(contacts), status=status.HTTP_200_OK)

    def post(self, request):
        if not request.user.is_authenticated:
            return Response({"error": "Authentication required"}, status=status.HTTP_401_UNAUTHORIZED)
        
        name = request.data.get('name')
        phone = request.data.get('phone_number')
        
        if not name or not phone:
            return Response({"error": "Name and phone required"}, status=status.HTTP_400_BAD_REQUEST)
            
        EmergencyContact.objects.create(user=request.user, name=name, phone_number=phone)
        return Response({"message": "Contact added"}, status=status.HTTP_201_CREATED)
