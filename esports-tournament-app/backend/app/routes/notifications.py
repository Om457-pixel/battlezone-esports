from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from ..database import get_db
from datetime import datetime

notifications_bp = Blueprint('notifications', __name__)

@notifications_bp.route('/', methods=['GET'])
@jwt_required()
def get_notifications():
    uid = get_jwt_identity()
    db = get_db()
    limit = int(request.args.get('limit', 20))
    notifs = list(db.notifications.find(
        {"user_id": uid}, {"_id": 0}
    ).sort("created_at", -1).limit(limit))
    for n in notifs:
        if isinstance(n.get('created_at'), datetime):
            n['created_at'] = n['created_at'].isoformat()
    return jsonify({"notifications": notifs}), 200

@notifications_bp.route('/mark-read', methods=['POST'])
@jwt_required()
def mark_read():
    uid = get_jwt_identity()
    data = request.get_json()
    notif_ids = data.get('ids', [])
    db = get_db()
    if notif_ids:
        db.notifications.update_many(
            {"user_id": uid, "notif_id": {"$in": notif_ids}},
            {"$set": {"read": True}}
        )
    else:
        db.notifications.update_many({"user_id": uid}, {"$set": {"read": True}})
    return jsonify({"success": True}), 200

@notifications_bp.route('/unread-count', methods=['GET'])
@jwt_required()
def unread_count():
    uid = get_jwt_identity()
    count = get_db().notifications.count_documents({"user_id": uid, "read": False})
    return jsonify({"count": count}), 200

def send_notification(user_id, title, body, notif_type, data=None):
    db = get_db()
    import uuid
    db.notifications.insert_one({
        "notif_id": str(uuid.uuid4()),
        "user_id": user_id,
        "title": title,
        "body": body,
        "type": notif_type,
        "data": data or {},
        "read": False,
        "created_at": datetime.utcnow()
    })
