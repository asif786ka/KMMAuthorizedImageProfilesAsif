//
//  AppNavGraph.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct AppNavGraph: View {
    @State private var securePreferences = SecurePreferences() // Initialize SecurePreferences

    var body: some View {
        NavigationView {
            if securePreferences.getToken() == nil {
                LoginScreen(container: .shared)
            } else {
                HomeScreen(container: .shared)
            }
        }
    }
}


