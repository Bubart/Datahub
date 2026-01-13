from fastapi import FastAPI
from pydantic import BaseModel
import random
import string
import requests
from datetime import datetime

app = FastAPI()

INGESTOR_URL = "http://localhost:8081/api/v1"

# Generate random partnerId, transactionId, format
def generate_random_data():
    formats = ['A', 'B', 'C']

    partner_id = random.randint(1,2) 
    transaction_id = ''.join(random.choices(string.ascii_uppercase + string.digits, k=10))
    format = random.choice(formats)
    timestamp = datetime.now().isoformat()

    return partner_id, transaction_id, format, timestamp


# Build XML from random data
def build_xml(partner_id, transaction_id, format, timestamp):
    return f"""
<PartnerMessage>
    <PartnerId>{partner_id}</PartnerId>
    <TransactionId>{transaction_id}</TransactionId>
    <Format>{format}</Format>
    <Timestamp>{timestamp}</Timestamp>
</PartnerMessage>
""".strip()


# ===================================================================
# 1️⃣  AUTO-GENERATE XML when opening the URL (NO sending)
# ===================================================================
@app.get("/generate-xml")
def generate_xml():
    partner_id, tx_id, format, ts = generate_random_data()
    xml_body = build_xml(partner_id, tx_id, format, ts)
    return {"generated_xml": xml_body}


# ===================================================================
# 2️⃣ AUTO-GENERATE XML AND SEND TO SPRING BOOT
# ===================================================================
@app.get("/generate-and-send")
def generate_and_send():

    i = 0

    for _ in range(10): 
        partner_id, tx_id, format, ts = generate_random_data()
        xml_body = build_xml(partner_id, tx_id, format, ts)

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
    partner_id, tx_id, format, ts = generate_random_data()
    xml_body = build_xml(partner_id, tx_id, format, ts)

    headers = {"Content-Type": "application/xml"}
    response = requests.post(INGESTOR_URL, data=xml_body, headers=headers)

    return {
        "xml_sent": xml_body,
        "status": response.status_code,
        "spring_response": response.text
    }







