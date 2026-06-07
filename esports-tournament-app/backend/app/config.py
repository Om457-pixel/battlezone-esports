import os
from datetime import timedelta
from dotenv import load_dotenv

load_dotenv()

class Config:
    SECRET_KEY = os.getenv('SECRET_KEY', 'dev-secret-key')
    JWT_SECRET_KEY = os.getenv('JWT_SECRET_KEY', 'jwt-secret-key')
    JWT_ACCESS_TOKEN_EXPIRES = timedelta(days=7)
    JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=30)
    MONGO_URI = os.getenv('MONGO_URI', 'mongodb://localhost:27017/esports_db')
    FIREBASE_CREDENTIALS = os.getenv('FIREBASE_CREDENTIALS')
    RAZORPAY_KEY_ID = os.getenv('RAZORPAY_KEY_ID')
    RAZORPAY_KEY_SECRET = os.getenv('RAZORPAY_KEY_SECRET')
    ADMIN_SECRET = os.getenv('ADMIN_SECRET', 'admin-secret')
    MAX_CONTENT_LENGTH = 16 * 1024 * 1024  # 16MB max upload
