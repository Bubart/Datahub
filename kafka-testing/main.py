from fastapi import FastAPI
from pydantic import BaseModel
import random
import string
import requests
from datetime import datetime

app = FastAPI()

INGESTOR_URL = "http://localhost:8081/api/v1"

# Generate random partnerId, transactionId, amount
def generate_random_data():
    partner_id = str(random.randint(1000, 9999))
    transaction_id = ''.join(random.choices(string.ascii_uppercase + string.digits, k=10))
    amount = round(random.uniform(10, 9999), 2)
    timestamp = datetime.now().isoformat()

    return partner_id, transaction_id, amount, timestamp


# Build XML from random data
def build_xml(partner_id, transaction_id, amount, timestamp):
    return f"""
<PartnerMessage>
    <PartnerId>{partner_id}</PartnerId>
    <TransactionId>{transaction_id}</TransactionId>
    <Amount>{amount}</Amount>
    <Timestamp>{timestamp}</Timestamp>
</PartnerMessage>
""".strip()


# ===================================================================
# 1️⃣  AUTO-GENERATE XML when opening the URL (NO sending)
# ===================================================================
@app.get("/generate-xml")
def generate_xml():
    partner_id, tx_id, amount, ts = generate_random_data()
    xml_body = build_xml(partner_id, tx_id, amount, ts)
    return {"generated_xml": xml_body}


# ===================================================================
# 2️⃣ AUTO-GENERATE XML AND SEND TO SPRING BOOT
# ===================================================================
@app.get("/generate-and-send")
def generate_and_send():

    i = 0

    for _ in range(500): 
        partner_id, tx_id, amount, ts = generate_random_data()
        xml_body = build_xml(partner_id, tx_id, amount, ts)

        headers = {"Content-Type": "application/xml"}
        response = requests.post(INGESTOR_URL, data=xml_body, headers=headers)

        i = i+ 1

        # return {
        #     "xml_sent": xml_body,
        #     "status": response.status_code,
        #     "spring_response": response.text
        # }

    return { "message" : "success", "count" : i }

@app.get("/send-one")
def send_one():
    partner_id, tx_id, amount, ts = generate_random_data()
    xml_body = build_xml(partner_id, tx_id, amount, ts)

    headers = {"Content-Type": "application/xml"}
    response = requests.post(INGESTOR_URL, data=xml_body, headers=headers)

    return {
        "xml_sent": xml_body,
        "status": response.status_code,
        "spring_response": response.text
    }







