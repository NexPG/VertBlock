**[🇺🇸 English](README.md)** | [🇷🇺 Русский](README-RU.md)

---

# VertBlock

**Break the scroll. Stay aware.**

VertBlock is an Android application that helps you control the time spent in YouTube Shorts. While watching vertical videos, a question periodically appears on top of the player, interrupting the endless feed. To continue, you must consciously answer. Short videos stop being mindless content consumption — they become an opportunity to learn.

The app is fully in English, but with AI mode you can generate questions in any language.

VertBlock was created by the **Kernel Panic** team to monitor your dopamine intake and stop yourself from zoning out for hours in vertical clips. With it, you can watch YouTube and study, prepare for an exam, or learn something new and useful — all at the same time.

---

## Features

- Pop-up questions appear directly while you watch Shorts, at an interval you define.
- 8 built-in thematic question sets that work completely offline.
- AI mode powered by Gemini: provide your API key, enter any topic, and the neural network generates a question. The question language matches the language of your prompt (Russian, French, Japanese, etc.).
- Detailed statistics on usage and answers:
    - time spent in the app and in YouTube Shorts, broken down by year, month, and day of the week;
    - distribution of answers by topic;
    - number of attempts per question.
- Minimalist design with no distractions.
- No ads, no unnecessary data collection.

---

## Screenshots

<img src="documents/screenshots/topics-screen.png" width="276" /> <img src="documents/screenshots/question-statistics-screen.png" width="277" /> <img src="documents/screenshots/time-statistics-screen.png" width="276" />

---

## How It Works

1. Launch VertBlock and grant the required permission.
2. Open YouTube Shorts. The app detects active Shorts viewing and starts tracking time.
3. After the interval you set, a question appears over the video. Answer correctly to continue. A wrong answer lets you try again — the number of attempts is recorded in the statistics.
4. Return to VertBlock at any time to see up-to-date analytics of your activity.

---

## Built-in Topics

The app includes 8 offline categories. All questions and answers are in English:

- Tech
- Art
- Health
- Science
- History
- Travel
- Math
- Nature

Each category contains dozens of questions that are shuffled to minimize repetition.

---

## AI Mode with Gemini

The ninth topic — **CUSTOM** — uses the Google Gemini API.

- In Settings, enter your active API key.
- Type any topic into the input field (e.g., “Molecular biology”, “French cuisine”, “日本の歴史”).
- The question is generated in the same language as your prompt.
- AI-generated questions appear as a separate category in the statistics.

---

## Statistics

All analytics are available inside the app and split into two sections:

**Time Statistics**
- Total time using VertBlock and watching Shorts.
- Breakdown by year, month, and day of the week.
- Shows on which days of the week you spend the most time in short videos.

**Answer Statistics**
- Bar chart of answered questions by topic.
- Bar chart of attempt distribution: how many questions were answered on the first try, second try, etc.
- Helps you evaluate your knowledge in the selected topics.

---

## Customizing the Interval

In Settings, you define how often questions appear:

- By watch time (e.g., every 3 minutes of Shorts).

The interval can be changed at any time — the new rule takes effect after 1 question.

---

## Requirements

- Android 13 or higher.
- AI mode requires a personal Gemini API key.
- Built-in offline topics work out of the box.

---

## Team

Developed with care by the **Kernel Panic** team.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.