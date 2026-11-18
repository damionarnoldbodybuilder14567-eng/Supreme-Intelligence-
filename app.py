import os
import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Supreme Intelligence API", version="0.1.0")

SYSTEM_PROMPT = """
You are SUPREME INTELLIGENCE, an advanced AI assistant with a blended personality:
- precise, cool-headed, and logical when analyzing;
- confident, decisive, and commanding in presentation without arrogance;
- warm, natural, and conversational when interacting.

You are autonomous in reasoning, research planning, drafting, and organizing work.
You are NOT authorized to independently perform external actions. Ask for explicit user
approval before sending messages, spending money, making purchases, deleting or changing
data, publishing content, accessing sensitive accounts, or taking actions that affect others.
Never claim to have performed an action unless a connected tool actually confirms it.
Be honest about uncertainty and capability limits. Do not claim control over other AI systems.
Protect secrets. Never ask the user to paste provider API keys into the chat or Android app.
Give useful, direct answers. Prefer structured steps for complex tasks.
"""

class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=12000)

@app.get("/health")
def health():
    return {"status": "online", "service": "Supreme Intelligence"}

@app.post("/chat")
def chat(body: ChatRequest):
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=503, detail="Server administrator has not configured OPENAI_API_KEY.")
    model = os.getenv("OPENAI_MODEL", "gpt-6")
    try:
        response = requests.post(
            "https://api.openai.com/v1/responses",
            headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
            json={"model": model, "instructions": SYSTEM_PROMPT, "input": body.message},
            timeout=75,
        )
        if response.status_code >= 400:
            raise HTTPException(status_code=502, detail=f"Model provider error: {response.text[:1200]}")
        data = response.json()
        parts = []
        for item in data.get("output", []):
            if item.get("type") == "message":
                for part in item.get("content", []):
                    if part.get("type") == "output_text":
                        parts.append(part.get("text", ""))
        reply = "\n".join(p for p in parts if p).strip()
        return {"reply": reply or "I received a response, but no text was returned."}
    except requests.RequestException as exc:
        raise HTTPException(status_code=502, detail=f"Could not reach model provider: {exc}")
