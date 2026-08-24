import urllib.request
import urllib.error
import json
import time

timestamp = int(time.time())

# 1. Signup passenger
signup_url = "http://localhost:8080/api/v1/auth/signup/passenger"
signup_data = json.dumps({
    "name": "Test Passenger",
    "email": f"passenger.{timestamp}@example.com",
    "password": "SecurePass@123",
    "phoneNumber": f"+9198{timestamp % 100000000:08d}"
}).encode('utf-8')

passenger_id = None
try:
    req = urllib.request.Request(signup_url, data=signup_data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as resp:
        passenger_res = json.loads(resp.read().decode())
        print("Passenger Signup Success:", passenger_res)
        passenger_id = passenger_res['id']
except Exception as e:
    print("Signup Error:", e)

if passenger_id:
    # 2. Create Booking
    booking_url = "http://localhost:8080/api/v1/booking"
    booking_data = json.dumps({
        "passengerId": passenger_id,
        "startLocation": {
            "latitude": 28.6139,
            "longitude": 77.2090
        },
        "endLocation": {
            "latitude": 28.5355,
            "longitude": 77.3910
        }
    }).encode('utf-8')

    try:
        req = urllib.request.Request(booking_url, data=booking_data, headers={'Content-Type': 'application/json'})
        with urllib.request.urlopen(req) as resp:
            print("Booking Status Code:", resp.status)
            print("Booking Response:", resp.read().decode())
    except urllib.error.HTTPError as e:
        print("Booking HTTP Error Code:", e.code)
        print("Error Body:", e.read().decode())
    except Exception as e:
        print("Booking General Error:", e)
