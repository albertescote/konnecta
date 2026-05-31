import {NextResponse} from "next/server";

// Replace <TEAM_ID> with your 10-character Apple Developer Team ID
// found at developer.apple.com → Membership
const TEAM_ID = "8GWF95H4H5";

const aasa = {
  applinks: {
    apps: [],
    details: [
      {
        appID: `${TEAM_ID}.com.konnecta.app`,
        paths: ["/join/*"],
      },
      {
        appID: `${TEAM_ID}.com.konnecta.app.dev`,
        paths: ["/join/*"],
      },
    ],
  },
};

export function GET() {
  return new NextResponse(JSON.stringify(aasa), {
    headers: {
      "Content-Type": "application/json",
    },
  });
}
