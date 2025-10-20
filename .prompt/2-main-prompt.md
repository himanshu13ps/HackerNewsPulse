### **Prompt for Native Android App: "Hacker News Pulse"**

#### **1. Core Concept & Goal**

Create a native Android application named **Hacker News Pulse**. The app will serve as a clean, fast, and user-friendly client for browsing stories from the Hacker News (HN) website. The primary goal is to provide a smooth, performant, and intuitive reading experience using a modern, card-based UI with efficient data handling and infinite scrolling, matching the aesthetic of the provided concept mock.

The application should be built using the latest Android development standards, specifically **Kotlin** and **Jetpack Compose**.

#### **2. Key Features & Functionality**

* **Story Fetching:** The app must fetch story data from the official Hacker News API.
* **Story Categories:** Users should be able to switch between viewing "Top Stories" and "New Stories."
* **Infinite Scrolling:** The story list must support continuous, bi-directional infinite scrolling to load more stories as the user scrolls down, and load older stories if the user scrolls back up.
* **Card-based UI:** Each story should be presented as a distinct card in a vertically scrolling list.
* **Web View:** Tapping on a story card should open the article's original URL in a browser. For a seamless user experience, implement this using **Chrome Custom Tabs**.

#### **3. UI/UX Design Details**

**a. Main Screen Layout:**

* **Background:** The entire screen background should feature a **light blue to dark blue gradient**, applied from the **top-left corner to the bottom-right corner**. (See mock for color inspiration).
* **Top App Bar (Visual Title & Selector, not a traditional AppBar component):**
    * **App Title:** Display `Hacker News` in a large, bold font on the top-left, matching the style and size in the mock. *Self-correction: The mock shows "Hacker News" as the primary title, not "Hacker News Pulse". We will use "Hacker News" as the prominent title based on the mock, assuming "Pulse" is more of a branding/technical name for the app rather than a visible part of the primary app title shown to the user.*
    * **Story Selector:** On the top-right, implement a pill-shaped, segmented selector as shown in the mock, allowing the user to switch between **"Top"** and **"New"** stories.
        * **Default View:** The **"Top"** stories view should be selected by default when the app first launches.
        * **Styling:** The selected tab should have a distinct background color (e.g., light blue as in the mock), while the unselected tab is less prominent. Text color should adapt for contrast.

* **Story List:**
    * A full-screen, vertically scrollable list of story cards, identical to the layout in the provided mock.
    * Ensure consistent spacing and padding around and between cards.

**b. Story Card Layout:**

Each card must match the visual design in the provided mock. They should be clean, rectangular with slightly rounded corners, and a subtle shadow/elevation to give them depth against the gradient background. The card should contain the following information, structured precisely as described and as seen in the mock:

* **Line 1: Title**
    * The full title of the story.
    * **Style:** Bold text, black/dark grey color, allowing for up to 3 lines before truncating with an ellipsis (`...`). Matches the font size and weight in the mock.

* **Line 2: Metadata Row 1** (below the title)
    * **Left-aligned:** The author's username, formatted as `by {author}`.
        * **Icon:** Precede the author's name with a small clock icon (`⏰`) as depicted in the mock. *Self-correction: The mock actually shows a clock icon for the publish date, not the author. Reverting to original prompt for author placement, but adding the clock for publish date as seen in the mock.*
    * **Right-aligned:** The source domain of the story's URL (e.g., `github.com`, `nytimes.com`). This requires parsing the domain from the story's `url` property. If the story has no URL (e.g., an "Ask HN" post), this can be omitted.
        * **Styling:** Smaller font size, lighter grey color compared to the title, right-aligned, as in the mock.

* **Line 3: Metadata Row 2** (below Metadata Row 1)
    * **Left-aligned:** The relative time since publication, formatted dynamically (e.g., `5 minutes ago`, `2 hours ago`, `3 days ago`).
        * **Icon:** Precede the publish date with a small clock icon (`⏰`) as depicted in the mock.
        * **Styling:** Same smaller font size and lighter grey color as the author.
    * **Right-aligned:** The story's score, formatted as `__{score}__ point/points` (e.g., `128 points`). The word should be "point" for a score of 1 and "points" otherwise.
        * **Styling:** Same smaller font size and lighter grey color as the author/date.

**c. Loading States:**

* **Initial Load:** When the app starts and the list is empty, display a loading indicator in the absolute center of the screen. It should consist of a spinning progress icon and the text: `Loading stories...` (Matching the text style of the app, e.g., white or light grey for contrast against the blue gradient).
* **Infinite Scroll Load:**
    * When scrolling **down** to fetch the next set of stories, display a loading indicator at the **bottom** of the list with a spinning icon and the text: `Loading more...`
    * When scrolling **up** to fetch a previous set of stories (that is no longer in memory), display a similar indicator at the **top** of the list.
    * *Self-correction: Ensure these loading messages are also styled to contrast with the background, similar to the initial load state.*

#### **4. Technical Specifications & Best Practices**

* **Language:** **Kotlin**
* **UI Toolkit:** **Jetpack Compose**
* **Architecture:** Use a modern, scalable architecture like **MVVM (Model-View-ViewModel)**.
* **Asynchronous Programming:** Use **Kotlin Coroutines and Flow** for managing background threads and handling data streams from the API.
* **Networking:** Use **Retrofit** for making API calls and **Moshi** or **kotlinx.serialization** for parsing JSON responses.
* **Dependency Injection:** Implement **Hilt** for managing dependencies.
* **Pagination:**
    * This is a critical requirement. Use the **Jetpack Paging 3 library** to implement the continuous/infinite scroll functionality.
    * Configure `PagingConfig` with a `pageSize` of **20** items.
    * Implement a `PagingSource` that fetches story IDs from the "top stories" or "new stories" endpoints and then fetches the details for each story.
    * The implementation should be robust enough to hold a reasonable number of items in memory (e.g., ~50-100) and discard older items that have been scrolled far away, preventing memory-related crashes.

#### **5. Hacker News API Endpoints**

* **Top Stories:** `https://hacker-news.firebaseio.com/v0/topstories.json`
* **New Stories:** `https://hacker-news.firebaseio.com/v0/newstories.json`
* **Item Details:** `https://hacker-news.firebaseio.com/v0/item/<story-id>.json`

**Workflow for fetching stories:**
1.  Fetch the list of story IDs from either the "top" or "new" endpoint.
2.  Use the Paging 3 library to paginate through this list of IDs.
3.  For each ID in the current page, make a separate API call to the "item" endpoint to get the full story details (`title`, `by`, `url`, `time`, `score`).
4.  Display the stories in the order returned by the initial API call ("Top" or "New").

***

#### **Response**
I have created all the necessary files for the "Hacker News Pulse" application. The project is now fully set up with a modern Android architecture using Kotlin,
  Jetpack Compose, Hilt, Retrofit, and the Paging 3 library.

  Here's a summary of what has been implemented:

   * Project Structure: All files are organized into appropriate packages for data, UI, dependency injection, and view models.
   * Build Configuration: The Gradle files (build.gradle.kts) are configured with all the required dependencies for the app to build and run.
   * Data Layer: The networking components (Retrofit, ApiService), data models, and the pagination logic (PagingSource) are all in place to fetch stories from the
     Hacker News API.
   * MVVM Architecture: The MainViewModel is set up to manage the application's state and business logic, providing a clean separation from the UI.
   * UI Layer: The UI has been built with Jetpack Compose, including:
       * The main screen with the specified blue gradient background.
       * The "Top"/"New" story type selector.
       * The card-based layout for displaying stories.
       * Loading indicators for initial load and infinite scrolling.
       * Click handlers to open stories in a Chrome Custom Tab.

  To build and run the application, you would typically open this project in Android Studio and press the "Run" button. Alternatively, you can use the Gradle
  wrapper from your terminal: