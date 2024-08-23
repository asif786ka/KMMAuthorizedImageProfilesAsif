//
//  HomeScreen.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

struct HomeScreen: View {
    @StateObject private var viewModel: HomeViewModel
    @State private var isProfileScreenActive = false // State to control navigation
    @State private var isLoggedOut = false // State to control logout navigation

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
                
                // Navigation to ProfileScreen
                NavigationLink(destination: ProfileScreen(container: .shared), isActive: $isProfileScreenActive) {
                    Button(action: {
                        isProfileScreenActive = true
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
        .navigationBarHidden(false) // Hide navigation bar when going back to login
    }
}


