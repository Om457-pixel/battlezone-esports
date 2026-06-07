from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, create_refresh_token, jwt_required, get_jwt_identity
from ..database import get_db
from ..models.user import create_user, get_user_by_uid
from datetime import datetime

auth_bp = Blueprint('auth', __name__)

def _init_firebase():
    """Lazy Firebase init — only if credentials are configured."""
    try:
        import firebase_admin
        from firebase_admin import auth as fb_auth, credentials
        if not firebase_admin._apps:
            import os
            cred_path = os.getenv('FIREBASE_CREDENTIALS')
            if cred_path and os.path.exists(cred_path):
                firebase_admin.initialize_app(credentials.Certificate(cred_path))
            else:
                firebase_admin.initialize_app()  # uses GOOGLE_APPLICATION_CREDENTIALS or ADC
        return fb_auth
    except Exception as e:
        return None

@auth_bp.route('/verify-token', methods=['POST'])
def verify_firebase_token():
    """Verify Firebase ID token and return JWT"""
    data = request.get_json()
    id_token = data.get('id_token')
    username = data.get('username')

    if not id_token:
        return jsonify({"error": "ID token required"}), 400

    fb_auth = _init_firebase()
    if fb_auth is None:
        return jsonify({"error": "Firebase not configured on server"}), 503

    try:
        decoded = fb_auth.verify_id_token(id_token)
        uid = decoded['uid']
        phone = decoded.get('phone_number')
        email = decoded.get('email')

        db = get_db()
        user = db.users.find_one({"uid": uid}, {"_id": 0})

        is_new_user = False
        if not user:
            if not username:
                return jsonify({"error": "Username required for new users", "new_user": True}), 200
            existing = db.users.find_one({"username": username})
            if existing:
                return jsonify({"error": "Username already taken"}), 409
            referred_by = data.get('referral_code')
            user = create_user(uid, username, phone=phone, email=email, referred_by=referred_by)
            is_new_user = True
            if referred_by:
                referrer = db.users.find_one({"referral_code": referred_by})
                if referrer:
                    db.users.update_one({"referral_code": referred_by}, {"$inc": {"bonus_balance": 50}})

        now = datetime.utcnow()
        last_login = user.get('last_login')
        streak = user.get('streak', 0)
        if last_login:
            diff = (now.date() - last_login.date()).days
            streak = streak + 1 if diff == 1 else (streak if diff == 0 else 1)
        db.users.update_one({"uid": uid}, {"$set": {"last_login": now, "streak": streak}})

        access_token = create_access_token(identity=uid)
        refresh_token = create_refresh_token(identity=uid)

        return jsonify({
            "access_token": access_token,
            "refresh_token": refresh_token,
            "user": user,
            "is_new_user": is_new_user
        }), 200

    except Exception as e:
        return jsonify({"error": f"Invalid token: {str(e)}"}), 401

@auth_bp.route('/refresh', methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    uid = get_jwt_identity()
    access_token = create_access_token(identity=uid)
    return jsonify({"access_token": access_token}), 200

@auth_bp.route('/check-username', methods=['GET'])
def check_username():
    username = request.args.get('username', '')
    if len(username) < 3:
        return jsonify({"available": False, "error": "Too short"}), 200
    try:
        db = get_db()
        exists = db.users.find_one({"username": username})
        return jsonify({"available": not bool(exists)}), 200
    except Exception:
        return jsonify({"available": True, "warning": "DB unavailable"}), 200

@auth_bp.route('/me', methods=['GET'])
@jwt_required()
def get_me():
    uid = get_jwt_identity()
    user = get_user_by_uid(uid)
    if not user:
        return jsonify({"error": "User not found"}), 404
    return jsonify(user), 200

@auth_bp.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "service": "BattleZone API"}), 200
