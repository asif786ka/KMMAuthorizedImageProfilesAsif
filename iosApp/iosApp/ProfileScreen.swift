//
//  ProfileScreen.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ProfileScreen: View {
    @StateObject private var viewModel: ProfileViewModel

    // Dependency injection through the initializer
    init(container: DependencyContainer = .shared) {
        let userRepository = container.provideUserRepository()
        let securePreferences = container.provideSecurePreferences()
        _viewModel = StateObject(wrappedValue: ProfileViewModel(repository: userRepository, securePreferences: securePreferences))
    }

    @State private var showImagePicker = false // Controls the display of the image picker
    @State private var showCamera = false // Controls the display of the camera picker
    @State private var selectedImage: UIImage? // Stores the selected or captured image

    var body: some View {
        VStack {
            switch viewModel.profileState {
            case .loading:
                ProgressView()
            case .success(let profile):
                ProfileCard(profile: profile, selectedImage: selectedImage)
                
                Button(action: {
                    showCamera = true
                }) {
                    Text("Take a Picture")
                }
                .padding()

                Button(action: {
                    showImagePicker = true
                }) {
                    Text("Pick from Gallery")
                }
                .padding()

                Button(action: {
                    if let image = selectedImage {
                        viewModel.updateAvatar(image: image)
                    }
                }) {
                    Text("Update Avatar")
                }
                .padding()

            case .error(let message):
                Text("Error: \(message)")
            }
        }
        .padding()
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: $selectedImage)
        }
        .sheet(isPresented: $showCamera) {
            CameraPicker(image: $selectedImage)
        }
        .onAppear {
            viewModel.loadProfile()
        }
        .navigationTitle("Profile") // Title of the Profile Screen
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarItems(leading: Button(action: {
            // Back action is handled automatically by NavigationView, so no need to implement
        }) {
            Image(systemName: "arrow.left")
                .foregroundColor(.blue)
        })
    }
}

