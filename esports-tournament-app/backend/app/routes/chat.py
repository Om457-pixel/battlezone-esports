from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_socketio import emit, join_room, leave_room
from .. import socketio
from ..database import get_db
from datetime import datetime

chat_bp = Blueprint('chat', __name__)

@chat_bp.route('/<match_id>/messages', methods=['GET'])
@jwt_required()
def get_messages(match_id):
    db = get_db()
    limit = int(request.args.get('limit', 50))
    messages = list(db.chat_messages.find(
        {"match_id": match_id}, {"_id": 0}
    ).sort("timestamp", -1).limit(limit))
    for m in messages:
        if isinstance(m.get('timestamp'), datetime):
            m['timestamp'] = m['timestamp'].isoformat()
    return jsonify({"messages": list(reversed(messages))}), 200

@socketio.on('join_match_chat')
def on_join(data):
    match_id = data.get('match_id')
    join_room(f"match_{match_id}")
    emit('status', {'msg': 'Joined match chat'}, room=f"match_{match_id}")

@socketio.on('leave_match_chat')
def on_leave(data):
    match_id = data.get('match_id')
    leave_room(f"match_{match_id}")

@socketio.on('send_message')
def on_message(data):
    db = get_db()
    match_id = data.get('match_id')
    user_id = data.get('user_id')
    message = data.get('message', '').strip()[:200]  # limit message length

    if not message:
        return

    user = db.users.find_one({"uid": user_id}, {"username": 1, "avatar": 1, "level": 1})
    msg = {
        "match_id": match_id,
        "user_id": user_id,
        "username": user['username'] if user else "Unknown",
        "avatar": user.get('avatar') if user else None,
        "level": user.get('level', 1) if user else 1,
        "message": message,
        "timestamp": datetime.utcnow()
    }
    db.chat_messages.insert_one(msg)
    msg['timestamp'] = msg['timestamp'].isoformat()
    del msg['_id']
    emit('new_message', msg, room=f"match_{match_id}")
