import socket
import threading

# Ports matching Java application.properties
PORTS = {
    5001: "Partner1",
    5002: "Partner2", 
    5003: "Default/PartnerAll"
}

HOST = "0.0.0.0"


def handle_client(conn, addr, port, partner_name):
    """Handle a single client connection."""
    print(f"\n[{partner_name}] Connected from {addr} on port {port}")
    try:
        while True:
            data = conn.recv(4096)
            if not data:
                print(f"[{partner_name}] Client {addr} closed connection on port {port}")
                break
            message = data.decode("utf-8")
            print(f"\n{'='*60}")
            print(f"[{partner_name}] Received XML on port {port}:")
            print(f"{'='*60}")
            print(message)
            print(f"{'='*60}\n")
    except ConnectionResetError:
        print(f"[{partner_name}] Connection reset by {addr}")
    except Exception as e:
        print(f"[{partner_name}] Error: {e}")
    finally:
        conn.close()


def start_server(port, partner_name):
    """Start a TCP server on the given port."""
    print(f"[{partner_name}] Starting TCP server on {HOST}:{port}")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((HOST, port))
        server.listen()
        print(f"[{partner_name}] Waiting for connections on port {port}...")
        
        while True:
            conn, addr = server.accept()
            # Handle each client in a new thread
            client_thread = threading.Thread(
                target=handle_client,
                args=(conn, addr, port, partner_name),
                daemon=True
            )
            client_thread.start()


def main():
    print("=" * 60)
    print("Multi-Port TCP Server")
    print("=" * 60)
    print("\nPort Mapping:")
    for port, name in PORTS.items():
        print(f"  Port {port} -> {name}")
    print("=" * 60 + "\n")

    threads = []
    for port, partner_name in PORTS.items():
        t = threading.Thread(
            target=start_server,
            args=(port, partner_name),
            daemon=True
        )
        t.start()
        threads.append(t)

    # Keep main thread alive
    try:
        for t in threads:
            t.join()
    except KeyboardInterrupt:
        print("\nShutting down servers...")


if __name__ == "__main__":
    main()