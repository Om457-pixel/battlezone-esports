import sys, os
# Add local site-packages if present (for environments where global install isn't on PATH)
_local_pkgs = os.path.join(os.path.dirname(__file__), 'site-packages')
if os.path.exists(_local_pkgs) and _local_pkgs not in sys.path:
    sys.path.insert(0, _local_pkgs)

from app import create_app, socketio

app = create_app()

if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=5000, debug=True, use_reloader=False, allow_unsafe_werkzeug=True)
