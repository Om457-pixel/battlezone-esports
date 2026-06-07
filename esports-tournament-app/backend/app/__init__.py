from flask import Flask
from flask_cors import CORS
from flask_jwt_extended import JWTManager
from flask_socketio import SocketIO
from .config import Config
from .database import init_db

socketio = SocketIO(cors_allowed_origins="*", async_mode='threading')
jwt = JWTManager()

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    CORS(app)
    jwt.init_app(app)
    socketio.init_app(app)
    init_db(app)

    from .routes.auth import auth_bp
    from .routes.matches import matches_bp
    from .routes.users import users_bp
    from .routes.wallet import wallet_bp
    from .routes.leaderboard import leaderboard_bp
    from .routes.rewards import rewards_bp
    from .routes.admin import admin_bp
    from .routes.chat import chat_bp
    from .routes.notifications import notifications_bp

    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    app.register_blueprint(matches_bp, url_prefix='/api/matches')
    app.register_blueprint(users_bp, url_prefix='/api/users')
    app.register_blueprint(wallet_bp, url_prefix='/api/wallet')
    app.register_blueprint(leaderboard_bp, url_prefix='/api/leaderboard')
    app.register_blueprint(rewards_bp, url_prefix='/api/rewards')
    app.register_blueprint(admin_bp, url_prefix='/api/admin')
    app.register_blueprint(chat_bp, url_prefix='/api/chat')
    app.register_blueprint(notifications_bp, url_prefix='/api/notifications')

    return app
