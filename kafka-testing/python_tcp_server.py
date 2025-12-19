import socket

HOST = "0.0.0.0"
PORT = 5001

print(f"Starting TCP server on {HOST}:{PORT}")

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen()

    print("Waiting on incomming connections...")

    while True:
        conn, addr = server.accept()
        print(f"\n Connected nu {addr}")

        while conn:
            data = conn.recv(4096)

            if not data:
                print("Client closed connection.")
                break

            message = data.decode("utf-8")
            print("Received XML")
            print(message)