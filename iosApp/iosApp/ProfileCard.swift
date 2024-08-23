//
//  ProfileCard.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

/// The SwiftUI view representing the profile card displaying the user's information.
struct ProfileCard: View {
    let profile: ProfileResponse // The user's profile information
    let selectedImage: UIImage? // The selected or captured image

    var body: some View {
        VStack {
            Text("Email: \(profile.email)")
                .padding()

            if let image = selectedImage {
                Image(uiImage: image)
                    .resizable()
                    .clipShape(Circle())
                    .frame(width: 200, height: 200)
                    .padding()
            } else if let avatarUrl = profile.avatar_url, let url = URL(string: avatarUrl) {
                AsyncImage(url: url) { image in
                    image
                        .resizable()
                        .clipShape(Circle())
                        .frame(width: 200, height: 200)
                } placeholder: {
                    Circle().fill(Color.gray)
                        .frame(width: 200, height: 200)
                }
            } else {
                Circle().fill(Color.gray)
                    .frame(width: 200, height: 200)
            }
        }
    }
}

