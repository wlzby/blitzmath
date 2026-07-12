import SwiftUI
import GoogleMobileAds
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        } else {
            print("Firebase configuration skipped: GoogleService-Info.plist is missing.")
        }
        GADMobileAds.sharedInstance().start(completionHandler: nil)
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
