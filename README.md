# 🍿 Netflix Clone: Smart Entertainment Streaming Platform

Netflix Clone is an interactive media discovery platform that focuses on content exploration through high-quality trailers and social engagement. It digitizes the "what to watch next" process by combining official previews with a robust community-driven feedback system.

## Target Audience
The platform is designed for viewers who want to explore new content through trailers before committing to a full watch. It serves as a social hub for movie and series fans to discuss titles, share sentiments, and track their personal favorites within a community of like-minded enthusiasts.

## Key Features
🎬 Trailer-Centric Experience: A dedicated viewing environment powered by YouTube Player integration, focusing on high-quality previews for every title.

💬 Interactive Sentiment System: Beyond simple text, comments include status reactions (Like, Love, Dislike) to visually represent user opinions.

🏅 Engagement Badges: An automated achievement system that awards titles like "Social User" or "Series Expert" based on your activity and comments.

🧠 Preference-Based Discovery: A personalized home feed that prioritizes genres you selected during registration (e.g., Action, Sci-Fi, Drama).

🌓 Customizable Visuals: Full support for Dark and Light modes, allowing users to switch themes based on their viewing environment.

📂 Dynamic Watchlists: Users can curate their own "Favorites" and "Liked" lists to keep track of interesting trailers for future reference.

📸 Social Profile Identity: Real-time profile management with custom image uploads and editable account details.

## Technical Stack
Language: Java.

UI: Jetpack Navigation & View Binding (ensuring a smooth, fragment-based single-activity flow).

Backend & Auth: Firebase Authentication & Cloud Firestore for real-time data sync.

Media Engine: YouTube Android Player API for seamless trailer playback.

Image Processing: Glide (handling poster rendering and circular profile photos).

Data Storage: SharedPreferences for theme persistence and local user preferences.
