# NutriTracker Frontend

A React + TypeScript + Vite application for tracking nutrition through AI-powered meal image analysis.

## Features

- 🔐 **User Authentication**: Register and login with email verification
- 📸 **Meal Image Upload**: Upload photos of your meals
- 🤖 **AI Analysis**: Automatic food detection and nutrition calculation using Gemini AI
- 📊 **Detailed Breakdown**: View calories, macros, and ingredient-level nutrition
- 🎨 **Modern UI**: Clean, responsive design with Tailwind CSS

## Tech Stack

- **React 19** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Tailwind CSS 4** - Styling
- **Fetch API** - HTTP requests

## Getting Started

### Prerequisites

- Node.js 18+ installed
- Backend server running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The app will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
npm run preview
```

## Project Structure

```
src/
├── components/
│   ├── AuthForm.tsx       # Login/Register form
│   └── MealUpload.tsx     # Meal upload and analysis display
├── services/
│   └── api.ts             # API service layer
├── types/
│   └── index.ts           # TypeScript type definitions
├── App.tsx                # Main app component
└── main.tsx               # App entry point
```

## API Integration

The frontend connects to the backend API at `http://localhost:8080/api`:

- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /meals/upload` - Upload meal image
- `GET /meals/{id}/analysis` - Get analysis results

## Usage Flow

1. **Register/Login**: Create an account or login with existing credentials
2. **Upload Image**: Click to select or drag-drop a meal image
3. **Analyze**: Click "Upload & Analyze" to process the image
4. **View Results**: See detected foods, nutrition summary, and ingredient breakdown

## Environment Variables

Update `src/services/api.ts` to change the API base URL:

```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Notes

- Email verification is required after registration
- JWT tokens are stored in localStorage
- Analysis typically takes 5-15 seconds depending on image complexity
- Supports Indian cuisine with specialized nutrition database
