# Looi AI

LOOI-style robot companion app powered by OpenRouter — no hardware required.

## Features
- Animated custom robot face (blinking, floating, mood expressions)
- Streaming chat via OpenRouter API (SSE)
- 8 model choices (Gemini Flash, GPT-4o, Claude, Llama, Mistral, DeepSeek…)
- 5 personality modes
- Dark space-themed UI

## Setup
1. Open in Android Studio
2. Set `local.properties` with your SDK path: `sdk.dir=/path/to/sdk`
3. Run on device (minSdk 26, Android 8+)

## First launch
Enter your OpenRouter API key at `https://openrouter.ai/keys` → paste it in the setup screen.

## CI/CD
Push to `main` → GitHub Actions builds debug APK automatically.
For signed release APK, add these repository secrets:
- `KEYSTORE_BASE64` — base64-encoded `.jks` keystore
- `KEY_ALIAS` — key alias
- `KEYSTORE_PASSWORD` — keystore password
- `KEY_PASSWORD` — key password
