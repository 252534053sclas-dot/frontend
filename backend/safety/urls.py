from django.urls import path
from .views import SOSView, EmergencyContactView

urlpatterns = [
    path('sos/', SOSView.as_view(), name='send_sos'),
    path('contacts/', EmergencyContactView.as_view(), name='emergency_contacts'),
]
