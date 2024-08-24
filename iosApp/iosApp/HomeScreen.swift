//
//  HomeScreen.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import AVFoundation // Import AVFoundation for camera access
import shared

struct HomeScreen: View {
    @StateObject private var viewModel: HomeViewModel
    @State private var isProfileScreenActive = false // State to control navigation
    @State private var isLoggedOut = false // State to control logout navigation
    @State private var cameraAccessDenied = false // Tracks if camera access was denied

    // Dependency injection through the initializer
    init(container: DependencyContainer = .shared) {
        let securePreferences = container.provideSecurePreferences()
        _viewModel = StateObject(wrappedValue: HomeViewModel(securePreferences: securePreferences))
    }

    var body: some View {
        NavigationView {
            VStack {
                Text("Home Screen")
                
                Button(action: {
                    viewModel.logout()
                    isLoggedOut = true // Set to true to trigger navigation to LoginScreen
                }) {
                    Text("Logout")
                }
                .padding()

                // Display a warning if camera access is denied
                if cameraAccessDenied {
                    Text("Camera access is denied. Please enable it in settings.")
                        .foregroundColor(.red)
                        .padding()
                }

                // Navigation to ProfileScreen
                NavigationLink(destination: ProfileScreen(container: .shared), isActive: $isProfileScreenActive) {
                    Button(action: {
                        checkCameraPermissions()
                    }) {
                        Text("Go to Profile Screen")
                    }
                }
                .padding()
                
                // Navigate back to the login screen if logged out
                NavigationLink(destination: LoginScreen(container: .shared), isActive: $isLoggedOut) {
                    EmptyView()
                }
            }
            .navigationTitle("Home")
        }
        .navigationBarHidden(true) // Hide navigation bar when going back to login
    }

    // Function to check camera permissions
    private func checkCameraPermissions() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)

        switch status {
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    DispatchQueue.main.async {
                        isProfileScreenActive = true
                    }
                } else {
                    DispatchQueue.main.async {
                        cameraAccessDenied = true
                    }
                }
            }
        case .restricted, .denied:
            cameraAccessDenied = true
        case .authorized:
            isProfileScreenActive = true
        @unknown default:
            cameraAccessDenied = true
        }
    }
}


