//
//  LoginScreen.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

/// The SwiftUI view representing the login screen.
/// The SwiftUI view representing the login screen.
struct LoginScreen: View {
    @StateObject private var viewModel: LoginViewModel

    // Dependency injection through the initializer
    init(container: DependencyContainer = .shared) {
        let userRepository = container.provideUserRepository()
        let securePreferences = container.provideSecurePreferences()
        _viewModel = StateObject(wrappedValue: LoginViewModel(repository: userRepository, securePreferences: securePreferences))
    }

    @State private var email: String = "" // User email input
    @State private var password: String = "" // User password input
    @State private var isRegistered = true // Tracks whether the user is registered or not
    @State private var showErrorDialog = false // Controls the display of error dialog
    @State private var errorMessage: String = "" // Stores the error message to display
    @State private var shouldNavigateToHome = false // Controls navigation to the Home screen

    var body: some View {
        VStack {
            if case .loading = viewModel.loginState {
                // Show a circular progress view when loading
                ProgressView("Loading...")
                    .progressViewStyle(CircularProgressViewStyle())
                    .padding()
            } else {
                // Show the login form
                TextField("Email", text: $email)
                    .padding()
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                SecureField("Password", text: $password)
                    .padding()
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                Button(action: {
                    if isRegistered {
                        viewModel.login(email: email, password: password)
                    } else {
                        viewModel.register(email: email, password: password)
                    }
                }) {
                    Text(isRegistered ? "Login" : "Register")
                }
                .padding()
                .disabled(email.isEmpty || password.isEmpty)
                .alert(isPresented: $showErrorDialog) {
                    Alert(
                        title: Text("Login Error"),
                        message: Text(errorMessage),
                        dismissButton: .default(Text("OK"))
                    )
                }
                
                // Use 'Button' instead of 'TextButton'
                Button(action: {
                    isRegistered.toggle()
                }) {
                    Text(isRegistered ? "No account? Register here" : "Already registered? Login here")
                }
                .padding()
            }
            
            // NavigationLink to HomeScreen
            NavigationLink(destination: HomeScreen(), isActive: $shouldNavigateToHome) {
                EmptyView()
            }
        }
        .padding()
        .onReceive(viewModel.$loginState) { state in
            switch state {
            case .success:
                // Navigate to Home Screen
                shouldNavigateToHome = true
                
            case .error(let message):
                errorMessage = message
                showErrorDialog = true
                
            default:
                break
            }
        }
    }
}
