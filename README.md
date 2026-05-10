# KONNECTA

KONNECTA is a modern Progressive Web App (PWA) designed for groups of friends to coordinate visits to their hometown and plan activities. It simplifies coordination with a clean, fast, and gesture-driven mobile interface.

## 🌐 Live Status

**Live Application:** [https://weekend-tracker-five.vercel.app/](https://weekend-tracker-five.vercel.app/)

KONNECTA is now a **multi-tenant** platform. You can create your own private groups, invite friends via secure links, and manage your community independently.

## ✨ Key Features

- **Authentication:** Secure login via Google OAuth and Magic Links using Supabase Auth.
- **Weekend Voting:** Users can mark themselves as "Going", "Not Going", or "Pending" for any upcoming weekend.
- **Activity Board:** Create and join specific plans (e.g., "Dinner on Saturday", "Padel match") associated with a weekend.
- **Multi-Tenancy (Groups):** Create multiple independent circles of friends. Each group has its own activities, members, and voting history.
- **Secure Temporal Invites:** Invite friends via unique UUID-based links that expire automatically after 48 hours for maximum security.
- **WhatsApp Integration:** Share activity details directly to WhatsApp groups with pre-filled messages and deep links.
- **Add to Calendar:** Save activities directly to Google Calendar or download an `.ics` file for iOS/Outlook.
- **Contextual Weather:** Real-time weather forecasts for the selected weekend to help plan activities.
- **Hall of Fame:** Gamified leaderboards to track who visits most frequently.
- **Push Notifications:** Stay updated when friends change their status or new plans are created.
- **PWA Ready:** Installable on iOS and Android for a native-like experience, including pull-to-refresh.
- **Dark Mode:** Full support for system-based or manual theme switching.

## 🚀 Tech Stack

- **Framework:** [Next.js 16 (App Router)](https://nextjs.org/)
- **Frontend:** [React 19](https://react.dev/) (with React Compiler enabled), [Tailwind CSS 4](https://tailwindcss.com/)
- **Backend/Auth:** [Supabase](https://supabase.com/) (PostgreSQL, Row Level Security, SSR)
- **Notifications:** [OneSignal](https://onesignal.com/) for Web Push Notifications
- **Deployment:** [Vercel](https://vercel.com/)
- **Language:** [TypeScript](https://www.typescript.org/)

## 📁 Project Structure

- `app/`: Next.js App Router pages and API routes.
  - `actions/`: Group-scoped Server Actions (mutations).
- `components/`: Modular UI components (SwipeContainer, PlansHub, GroupModal, etc.).
- `lib/`: Shared utilities (OneSignal, Calendar, Date logic).
  - `supabase/`: Server and Client-side Supabase configuration.
- `types/`: Centralized TypeScript definitions.
- `public/`: Static assets and PWA/OneSignal service workers.

## 🛠️ Getting Started

### Prerequisites

- Node.js 18+
- A Supabase project
- A OneSignal app (for notifications)

### Environment Variables

Create a `.env.local` file in the root directory:

```env
# Supabase
NEXT_PUBLIC_SUPABASE_URL=your_supabase_url
NEXT_PUBLIC_SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key

# OneSignal
NEXT_PUBLIC_ONESIGNAL_APP_ID=your_onesignal_app_id
ONESIGNAL_REST_API_KEY=your_onesignal_rest_api_key

# Cron jobs
CRON_SECRET=your_cron_job_secret

# Testing
SILENT_NOTIFICATIONS=false

# Required for Group Creation
GROUP_CREATION_SECRET=your_admin_secret_key
```

### Installation

1. Clone the repository
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```

### Database Schema

The project expects the following tables in Supabase:

- `groups`: ID, Name, Slug, Invite Token, Expiration.
- `group_memberships`: User ID, Group ID, Role (admin/member).
- `profiles`: User-facing info (Name, Avatar, Email).
- `activities`: Title, Desc, Start/End Date, Start/End Time, Group ID.
- `activity_participants`: Join table for users attending specific activities.
- `weekend_plans`: User status per weekend, scoped by Group ID.

---
Built with ❤️ for the crew.
