# Bible API Configuration

## Primary Bible API

## Base URL:
https://bible-api.com/

## Endpoints

## Example fetch
https://bible-api.com/jn%203:16

### Get Books
GET /books

### Get Chapters
GET /chapters/{bookId}

### Get Verses
GET /john/{3}

### Search
GET https://bible-api.com/jn%203:16 

## Authentication

API Key Header:
Authorization: Bearer <API_KEY>

## Response Rules

- Responses use JSON
- Use Kotlin Serialization
- Handle nullable fields safely

## Retry Rules

- Retry 3 times on timeout
- Use exponential backoff
- Avoid duplicate calls

## Offline Rules

- Cache:
    - books
    - chapters
    - verses
- Support offline reading
- Sync bookmarks when online

## Performance Rules

- Lazy load chapters
- Paginate search results
- Avoid fetching full Bible at startup

## Error Handling

- Handle:
    - network failures
    - invalid responses
    - empty verses
    - rate limits

## Future Features

- AI verse explanations
- Semantic verse search
- Devotional generation
- Multi-language support