Bug fixes:

- The relative time since publication on card, formatted dynamically (e.g., `5 minutes ago`, `2 hours ago`, `3 days ago`) is showing plural for single items (e.g., `1 minutes ago`, `1 hours ago`, `1 days ago`). Please update it to show plural for plural items and singular for singular items
e.g., `1 minute ago`, `1 hour ago`, `1 day ago`, `5 minutes ago`, `2 hours ago`, `3 days ago`


- When clicking from 'top' to 'new' and viceversa, the existing list of stories should be cleaned up and "Loading Stories..." should be shown while new list is fetched. Currently it continues to show older rendered stories until new are recieved, leading to abad experience of user feeling that nothing happened on clicking the selector.

