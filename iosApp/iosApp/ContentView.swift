import UIKit
import SwiftUI
import ComposeApp
import GoogleMobileAds

class SwiftAdController: NSObject, IAdController {
    private var interstitial: GADInterstitialAd?
    private var rewardedAd: GADRewardedAd?
    
    // MARK: - AdMob Ad Unit Configurations
    // Set this to false for production release
    private let isDebug = true
    
    private var interstitialAdUnitID: String {
        return isDebug ? "ca-app-pub-3940256099942544/1033173712" : "YOUR_IOS_PRODUCTION_INTERSTITIAL_AD_UNIT_ID"
    }
    
    private var rewardedAdUnitID: String {
        return isDebug ? "ca-app-pub-3940256099942544/1712485313" : "YOUR_IOS_PRODUCTION_REWARDED_AD_UNIT_ID"
    }
    
    override init() {
        super.init()
        loadInterstitial()
        loadRewarded()
    }
    
    func loadInterstitial() {
        let request = GADRequest()
        GADInterstitialAd.load(withAdUnitID: interstitialAdUnitID, request: request) { [weak self] ad, error in
            if let error = error {
                print("Failed to load interstitial ad: \(error.localizedDescription)")
                return
            }
            self?.interstitial = ad
        }
    }
    
    func loadRewarded() {
        let request = GADRequest()
        GADRewardedAd.load(withAdUnitID: rewardedAdUnitID, request: request) { [weak self] ad, error in
            if let error = error {
                print("Failed to load rewarded ad: \(error.localizedDescription)")
                return
            }
            self?.rewardedAd = ad
        }
    }
    
    func showInterstitialAd(onClosed: @escaping () -> Void) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else {
                onClosed()
                return
            }
            let windows = UIApplication.shared.windows
            let window = UIApplication.shared.keyWindow ?? windows.first
            guard let rootVC = window?.rootViewController, let interstitial = self.interstitial else {
                print("Interstitial ad not ready or rootViewController missing")
                onClosed()
                return
            }
            
            interstitial.fullScreenContentDelegate = AdDelegate(
                onClosed: onClosed,
                onReload: { [weak self] in self?.loadInterstitial() }
            )
            interstitial.present(fromRootViewController: rootVC)
        }
    }
    
    func showRewardedAd(placement: AdPlacement, onReward: @escaping () -> Void, onClosed: @escaping () -> Void) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else {
                onClosed()
                return
            }
            let windows = UIApplication.shared.windows
            let window = UIApplication.shared.keyWindow ?? windows.first
            guard let rootVC = window?.rootViewController, let rewardedAd = self.rewardedAd else {
                print("Rewarded ad not ready or rootViewController missing. Simulation fallback.")
                onReward()
                onClosed()
                return
            }
            
            rewardedAd.fullScreenContentDelegate = AdDelegate(
                onClosed: onClosed,
                onReload: { [weak self] in self?.loadRewarded() }
            )
            rewardedAd.present(fromRootViewController: rootVC) {
                onReward()
            }
        }
    }
}

class AdDelegate: NSObject, GADFullScreenContentDelegate {
    let onClosed: () -> Void
    let onReload: () -> Void
    
    init(onClosed: @escaping () -> Void, onReload: @escaping () -> Void) {
        self.onClosed = onClosed
        self.onReload = onReload
    }
    
    func adDidDismissFullScreenContent(_ ad: GADFullScreenPresentingAd) {
        onClosed()
        onReload()
    }
    
    func ad(_ ad: GADFullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("Ad failed to present: \(error.localizedDescription)")
        onClosed()
        onReload()
    }
}

import FirebaseFirestore

class SwiftMultiplayerController: NSObject, IMultiplayerController {
    private let db = Firestore.firestore()
    private var lobbyListener: ListenerRegistration?
    
    private var matchmakingTimer: Timer?

    func startMatchmaking(
        playerId: String,
        playerName: String,
        level: Int32,
        country: String,
        onMatched: @escaping (String, KotlinInt, String, KotlinInt, String, KotlinLong, KotlinLong) -> Void
    ) {
        let validPlayerId = playerId.isEmpty ? UUID().uuidString : playerId
        cancelMatchmaking(playerId: validPlayerId)
        
        let myCreatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        let myTicketRef = db.collection("vs_queue").document(validPlayerId)
        let myTicketData: [String: Any] = [
            "playerId": validPlayerId,
            "playerName": playerName,
            "level": level,
            "country": country,
            "status": "searching",
            "matchedLobbyId": "",
            "createdAt": myCreatedAt
        ]
        
        myTicketRef.setData(myTicketData) { [weak self] error in
            guard let self = self else { return }
            if let error = error {
                print("SwiftMultiplayer: Failed to create ticket: \(error.localizedDescription)")
                return
            }
            
            // Listen to our own ticket for match status updates (if someone else matches us)
            self.lobbyListener = myTicketRef.addSnapshotListener { [weak self] snap, err in
                guard let self = self else { return }
                if let err = err {
                    print("SwiftMultiplayer: Error listening to ticket: \(err.localizedDescription)")
                    return
                }
                if let snap = snap, snap.exists, let data = snap.data() {
                    let status = data["status"] as? String ?? "searching"
                    let lobbyId = data["matchedLobbyId"] as? String ?? ""
                    if status == "matched", !lobbyId.isEmpty {
                        var attempts = 0
                        let fetchLobby: () -> Void = {
                            self.db.collection("vs_lobbies").document(lobbyId).getDocument { [weak self] lobbySnap, lobbyErr in
                                guard let self = self else { return }
                                if let lobbySnap = lobbySnap, lobbySnap.exists, let lobbyData = lobbySnap.data() {
                                    let p2Name = lobbyData["player2Name"] as? String ?? ""
                                    let p2Level = lobbyData["player2Level"] as? Int32 ?? 1
                                    let p2Country = lobbyData["player2Country"] as? String ?? "US"
                                    let startTime = lobbyData["gameStartTimestamp"] as? Int64 ?? 0
                                    let seed = lobbyData["seed"] as? Int64 ?? 0
                                    
                                    self.cancelMatchmaking(playerId: validPlayerId)
                                    onMatched(lobbyId, KotlinInt(value: 1), p2Name, KotlinInt(value: p2Level), p2Country, KotlinLong(value: seed), KotlinLong(value: startTime))
                                } else if attempts < 5 {
                                    attempts += 1
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                                        fetchLobby()
                                    }
                                }
                            }
                        }
                        fetchLobby()
                    }
                }
            }
            
            // Start polling search loop for older tickets (every 1.0 second)
            self.matchmakingTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
                guard let self = self else { return }
                self.db.collection("vs_queue")
                    .whereField("status", isEqualTo: "searching")
                    .getDocuments { [weak self] querySnap, err in
                        guard let self = self else { return }
                        if err != nil { return }
                        guard let docs = querySnap?.documents else { return }
                        
                        var suitableDoc: QueryDocumentSnapshot?
                        var oldestCreatedAt: Int64 = Int64.max
                        
                        for doc in docs {
                            let docId = doc.documentID
                            if docId == validPlayerId { continue }
                            
                            let data = doc.data()
                            let docCreatedAt = data["createdAt"] as? Int64 ?? 0
                            
                            if (docCreatedAt < myCreatedAt || (docCreatedAt == myCreatedAt && docId < validPlayerId)) {
                                if docCreatedAt < oldestCreatedAt {
                                    oldestCreatedAt = docCreatedAt
                                    suitableDoc = doc
                                }
                            }
                        }
                        
                        if let match = suitableDoc {
                            let oppId = match.documentID
                            let oppName = match.data()["playerName"] as? String ?? ""
                            let oppLevel = match.data()["level"] as? Int32 ?? 1
                            let oppCountry = match.data()["country"] as? String ?? "US"
                            
                            let lobbyId = String(UUID().uuidString.prefix(8))
                            let seed = Int64.random(in: 1...1000000000)
                            let startTime = Int64(Date().timeIntervalSince1970 * 1000) + 3500
                            
                            let lobbyData: [String: Any] = [
                                "lobbyId": lobbyId,
                                "player1Id": oppId,
                                "player1Name": oppName,
                                "player1Level": oppLevel,
                                "player1Country": oppCountry,
                                "player1Score": 0,
                                "player2Id": validPlayerId,
                                "player2Name": playerName,
                                "player2Level": level,
                                "player2Country": country,
                                "player2Score": 0,
                                "status": "active",
                                "seed": seed,
                                "currentQuestionIndex": 0,
                                "lastAnswererId": "",
                                "gameStartTimestamp": startTime,
                                "createdAt": FieldValue.serverTimestamp()
                            ]
                            
                            let olderRef = self.db.collection("vs_queue").document(oppId)
                            let newerRef = self.db.collection("vs_queue").document(validPlayerId)
                            let lobbyRef = self.db.collection("vs_lobbies").document(lobbyId)
                            
                            self.db.runTransaction({ (transaction, errorPointer) -> Any? in
                                do {
                                    let snapOlder = try transaction.getDocument(olderRef)
                                    let snapNewer = try transaction.getDocument(newerRef)
                                    
                                    let statusOlder = snapOlder.data()?["status"] as? String ?? "searching"
                                    let statusNewer = snapNewer.data()?["status"] as? String ?? "searching"
                                    
                                    if statusOlder == "searching" && statusNewer == "searching" {
                                        transaction.updateData(["status": "matched", "matchedLobbyId": lobbyId], forDocument: olderRef)
                                        transaction.updateData(["status": "matched", "matchedLobbyId": lobbyId], forDocument: newerRef)
                                        transaction.setData(lobbyData, forDocument: lobbyRef)
                                        return true
                                    }
                                } catch {}
                                return false
                            }) { [weak self] (result, transactionErr) in
                                guard let self = self else { return }
                                let success = result as? Bool ?? false
                                if success {
                                    self.cancelMatchmaking(playerId: validPlayerId)
                                    onMatched(lobbyId, KotlinInt(value: 2), oppName, KotlinInt(value: oppLevel), oppCountry, KotlinLong(value: seed), KotlinLong(value: startTime))
                                }
                            }
                        }
                    }
            }
        }
    }
    
    func cancelMatchmaking(playerId: String) {
        matchmakingTimer?.invalidate()
        matchmakingTimer = nil
        lobbyListener?.remove()
        lobbyListener = nil
        if !playerId.isEmpty {
            db.collection("vs_queue").document(playerId).delete()
        }
    }
    
    func observeLobby(
        lobbyId: String,
        onUpdate: @escaping (LobbyState) -> Void,
        onError: @escaping (String) -> Void
    ) {
        lobbyListener?.remove()
        lobbyListener = db.collection("vs_lobbies").document(lobbyId)
            .addSnapshotListener { docSnap, error in
                if let error = error {
                    onError(error.localizedDescription)
                    return
                }
                guard let doc = docSnap, doc.exists, let data = doc.data() else {
                    onError("Lobby missing")
                    return
                }
                
                let status: String = data["status"] as? String ?? ""
                let player1Score: Int64 = data["player1Score"] as? Int64 ?? 0
                let player2Score: Int64 = data["player2Score"] as? Int64 ?? 0
                let currentQuestionIndex: Int64 = data["currentQuestionIndex"] as? Int64 ?? 0
                let lastAnswererId: String = data["lastAnswererId"] as? String ?? ""
                let p1Emote: String? = data["p1Emote"] as? String
                let p2Emote: String? = data["p2Emote"] as? String
                let rematchP1: Bool = data["rematchP1"] as? Bool ?? false
                let rematchP2: Bool = data["rematchP2"] as? Bool ?? false
                let gameStartTimestamp: Int64 = data["gameStartTimestamp"] as? Int64 ?? 0
                
                let state = LobbyState(
                    status: status,
                    player1Score: player1Score,
                    player2Score: player2Score,
                    currentQuestionIndex: currentQuestionIndex,
                    lastAnswererId: lastAnswererId,
                    p1Emote: p1Emote,
                    p2Emote: p2Emote,
                    rematchP1: rematchP1,
                    rematchP2: rematchP2,
                    gameStartTimestamp: gameStartTimestamp
                )
                onUpdate(state)
            }
    }
    
    func stopObservingLobby() {
        lobbyListener?.remove()
        lobbyListener = nil
    }
    
    func updateScore(lobbyId: String, role: Int32, score: Int64) {
        let field = role == 1 ? "player1Score" : "player2Score"
        db.collection("vs_lobbies").document(lobbyId).updateData([field: score])
    }
    
    func sendEmote(lobbyId: String, role: Int32, emoteText: String) {
        let field = role == 1 ? "p1Emote" : "p2Emote"
        let payload = "\(emoteText)|\(Int64(Date().timeIntervalSince1970 * 1000))"
        db.collection("vs_lobbies").document(lobbyId).updateData([field: payload])
    }
    
    func requestRematch(lobbyId: String, role: Int32, request: Bool) {
        let field = role == 1 ? "rematchP1" : "rematchP2"
        db.collection("vs_lobbies").document(lobbyId).updateData([field: request])
    }
    
    func updateLobbyStatus(lobbyId: String, status: String) {
        db.collection("vs_lobbies").document(lobbyId).updateData(["status": status])
    }
    
    func deleteLobby(lobbyId: String) {
        db.collection("vs_lobbies").document(lobbyId).delete()
    }
    
    func acceptRematch(lobbyId: String, role: Int32) {
        let lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        let updateField = role == 1 ? "rematchP1" : "rematchP2"
        
        db.runTransaction({ (transaction, errorPointer) -> Any? in
            do {
                let snap = try transaction.getDocument(lobbyRef)
                let p1Rematch = role == 1 ? true : (snap.data()?["rematchP1"] as? Bool ?? false)
                let p2Rematch = role == 2 ? true : (snap.data()?["rematchP2"] as? Bool ?? false)
                
                transaction.updateData([updateField: true], forDocument: lobbyRef)
                
                if p1Rematch && p2Rematch {
                    transaction.updateData([
                        "status": "active",
                        "player1Score": 0,
                        "player2Score": 0,
                        "p1Emote": NSNull(),
                        "p2Emote": NSNull(),
                        "rematchP1": false,
                        "rematchP2": false,
                        "currentQuestionIndex": 0,
                        "gameStartTimestamp": Int64(Date().timeIntervalSince1970 * 1000) + 3500
                    ], forDocument: lobbyRef)
                }
            } catch {
                print("SwiftMultiplayer: Failed to get lobby doc in acceptRematch")
            }
            return nil
        }) { _, _ in }
    }
    
    func submitCorrectAnswer(lobbyId: String, role: Int32, playerId: String, questionIndex: Int64, onResult: @escaping (KotlinBoolean) -> Void) {
        let lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        db.runTransaction({ (transaction, errorPointer) -> Any? in
            do {
                let snap = try transaction.getDocument(lobbyRef)
                let dbIndex = snap.data()?["currentQuestionIndex"] as? Int64 ?? 0
                if dbIndex == questionIndex {
                    let scoreField = role == 1 ? "player1Score" : "player2Score"
                    let currentScore = snap.data()?[scoreField] as? Int64 ?? 0
                    transaction.updateData([
                        "currentQuestionIndex": dbIndex + 1,
                        scoreField: currentScore + 10,
                        "lastAnswererId": playerId
                    ], forDocument: lobbyRef)
                    return true
                }
            } catch {}
            return false
        }) { (result, _) in
            let res = result as? Bool ?? false
            onResult(KotlinBoolean(value: res))
        }
    }
    
    func submitWrongAnswer(lobbyId: String, role: Int32, questionIndex: Int64, onResult: @escaping (KotlinBoolean) -> Void) {
        let lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        db.runTransaction({ (transaction, errorPointer) -> Any? in
            do {
                let snap = try transaction.getDocument(lobbyRef)
                let dbIndex = snap.data()?["currentQuestionIndex"] as? Int64 ?? 0
                if dbIndex == questionIndex {
                    let myWrongField = role == 1 ? "player1WrongIndex" : "player2WrongIndex"
                    let oppWrongField = role == 1 ? "player2WrongIndex" : "player1WrongIndex"
                    
                    let oppWrongIndex = snap.data()?[oppWrongField] as? Int64 ?? -1
                    if oppWrongIndex == dbIndex {
                        transaction.updateData([
                            myWrongField: dbIndex,
                            "currentQuestionIndex": dbIndex + 1
                        ], forDocument: lobbyRef)
                    } else {
                        transaction.updateData([myWrongField: dbIndex], forDocument: lobbyRef)
                    }
                    return true
                }
            } catch {}
            return false
        }) { (result, _) in
            let res = result as? Bool ?? false
            onResult(KotlinBoolean(value: res))
        }
    }
    
    func advanceQuestionIndex(lobbyId: String, currentIndex: Int64, onResult: @escaping (KotlinBoolean) -> Void) {
        let lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        db.runTransaction({ (transaction, errorPointer) -> Any? in
            do {
                let snap = try transaction.getDocument(lobbyRef)
                let dbIndex = snap.data()?["currentQuestionIndex"] as? Int64 ?? 0
                if dbIndex == currentIndex {
                    transaction.updateData(["currentQuestionIndex": dbIndex + 1], forDocument: lobbyRef)
                    return true
                }
            } catch {}
            return false
        }) { (result, _) in
            let res = result as? Bool ?? false
            onResult(KotlinBoolean(value: res))
        }
    }
    
    func createCustomRoom(
        playerId: String,
        playerName: String,
        level: Int32,
        country: String,
        onRoomCreated: @escaping (String) -> Void,
        onMatched: @escaping (String, KotlinInt, String, KotlinInt, String, KotlinLong, KotlinLong) -> Void,
        onError: @escaping (String) -> Void
    ) {
        let validPlayerId = playerId.isEmpty ? UUID().uuidString : playerId
        let roomCode = generateRoomCode()
        onRoomCreated(roomCode)
        
        let roomRef = db.collection("vs_lobbies").document("room_" + roomCode)
        let roomData: [String: Any] = [
            "roomCode": roomCode,
            "hostId": validPlayerId,
            "hostName": playerName,
            "hostLevel": level,
            "hostCountry": country,
            "guestId": "",
            "guestName": "",
            "guestLevel": 1,
            "guestCountry": "US",
            "status": "waiting",
            "matchedLobbyId": "",
            "seed": Int64(0),
            "gameStartTimestamp": Int64(0),
            "createdAt": FieldValue.serverTimestamp()
        ]
        
        roomRef.setData(roomData) { [weak self] error in
            guard let self = self else { return }
            if let error = error {
                onError("Oda oluşturulamadı: \(error.localizedDescription)")
                return
            }
        }
        
        self.lobbyListener = roomRef.addSnapshotListener { [weak self] snap, err in
            guard let self = self else { return }
            if let err = err {
                onError("Oda hatası: \(err.localizedDescription)")
                return
            }
            if let snap = snap, snap.exists, let data = snap.data() {
                let status = data["status"] as? String ?? "waiting"
                let lobbyId = data["matchedLobbyId"] as? String ?? ""
                if status == "matched", !lobbyId.isEmpty {
                    let guestName = data["guestName"] as? String ?? "Konuk"
                    let guestLevel = data["guestLevel"] as? Int32 ?? 1
                    let guestCountry = data["guestCountry"] as? String ?? "US"
                    let seed = data["seed"] as? Int64 ?? 0
                    let startTime = data["gameStartTimestamp"] as? Int64 ?? 0
                    
                    self.lobbyListener?.remove()
                    self.lobbyListener = nil
                    onMatched(lobbyId, KotlinInt(value: 1), guestName, KotlinInt(value: guestLevel), guestCountry, KotlinLong(value: seed), KotlinLong(value: startTime))
                }
            }
        }
    }
    
    func joinCustomRoom(
        roomCode: String,
        playerId: String,
        playerName: String,
        level: Int32,
        country: String,
        onMatched: @escaping (String, KotlinInt, String, KotlinInt, String, KotlinLong, KotlinLong) -> Void,
        onError: @escaping (String) -> Void
    ) {
        let cleanCode = roomCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        if cleanCode.count != 4 {
            onError("Lütfen 4 haneli oda kodunu girin!")
            return
        }
        
        let validPlayerId = playerId.isEmpty ? UUID().uuidString : playerId
        let roomRef = db.collection("vs_lobbies").document("room_" + cleanCode)
        
        roomRef.getDocument { [weak self] snap, err in
            guard let self = self else { return }
            if let err = err {
                onError("Oda bilgisi alınamadı: \(err.localizedDescription)")
                return
            }
            guard let snap = snap, snap.exists, let data = snap.data() else {
                onError("Oda bulunamadı! Lütfen kodu kontrol edin.")
                return
            }
            
            let status = data["status"] as? String ?? ""
            if status != "waiting" {
                onError("Bu oda dolu veya kapanmış!")
                return
            }
            
            let hostId = data["hostId"] as? String ?? ""
            if hostId == validPlayerId {
                onError("Kendi oluşturduğunuz odaya katılamazsınız!")
                return
            }
            
            let hostName = data["hostName"] as? String ?? "Ev Sahibi"
            let hostLevel = data["hostLevel"] as? Int32 ?? 1
            let hostCountry = data["hostCountry"] as? String ?? "US"
            
            let lobbyId = String(UUID().uuidString.prefix(8))
            let seed = Int64.random(in: 1...1000000000)
            let startTime = Int64(Date().timeIntervalSince1970 * 1000) + 3500
            let lobbyRef = self.db.collection("vs_lobbies").document(lobbyId)
            
            let lobbyData: [String: Any] = [
                "lobbyId": lobbyId,
                "player1Id": hostId,
                "player1Name": hostName,
                "player1Level": hostLevel,
                "player1Country": hostCountry,
                "player1Score": 0,
                "player2Id": validPlayerId,
                "player2Name": playerName,
                "player2Level": level,
                "player2Country": country,
                "player2Score": 0,
                "status": "active",
                "seed": seed,
                "currentQuestionIndex": 0,
                "lastAnswererId": "",
                "gameStartTimestamp": startTime,
                "createdAt": FieldValue.serverTimestamp()
            ]
            
            self.db.runTransaction({ (transaction, errorPointer) -> Any? in
                do {
                    let roomSnap = try transaction.getDocument(roomRef)
                    let currentStatus = roomSnap.data()?["status"] as? String ?? ""
                    if currentStatus != "waiting" {
                        return false
                    }
                    
                    transaction.updateData([
                        "status": "matched",
                        "matchedLobbyId": lobbyId,
                        "guestId": validPlayerId,
                        "guestName": playerName,
                        "guestLevel": level,
                        "guestCountry": country,
                        "seed": seed,
                        "gameStartTimestamp": startTime
                    ], forDocument: roomRef)
                    
                    transaction.setData(lobbyData, forDocument: lobbyRef)
                    return true
                } catch {
                    return false
                }
            }) { (result, error) in
                let success = result as? Bool ?? false
                if success {
                    onMatched(lobbyId, KotlinInt(value: 2), hostName, KotlinInt(value: hostLevel), hostCountry, KotlinLong(value: seed), KotlinLong(value: startTime))
                } else {
                    onError("Odaya katılırken hata oluştu!")
                }
            }
        }
    }
    
    func cancelCustomRoom(roomCode: String, playerId: String) {
        lobbyListener?.remove()
        lobbyListener = nil
        if !roomCode.isEmpty {
            db.collection("vs_lobbies").document("room_" + roomCode.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()).delete()
        }
    }
    
    private func generateRoomCode() -> String {
        let digits = "0123456789"
        return String((0..<4).compactMap { _ in digits.randomElement() })
    }
}

extension DocumentSnapshot {
    func string(forKey key: String) -> String {
        return data()?[key] as? String ?? ""
    }
    func int32(forKey key: String) -> Int32 {
        return data()?[key] as? Int32 ?? 0
    }
    func int64(forKey key: String) -> Int64 {
        return data()?[key] as? Int64 ?? 0
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let adController: IAdController
    let multiplayerController: IMultiplayerController
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            customAdController: adController,
            customMultiplayerController: multiplayerController
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @StateObject private var adManager = AdManager()
    private let multiplayerController = SwiftMultiplayerController()

    var body: some View {
        ComposeView(
            adController: adManager.adController,
            multiplayerController: multiplayerController
        )
        .ignoresSafeArea()
    }
}

class AdManager: ObservableObject {
    let adController = SwiftAdController()
}



