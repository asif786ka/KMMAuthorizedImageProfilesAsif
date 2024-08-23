//
//  ApiResponse.swift
//  iosApp
//
//  Created by AsifMacMini on 23/08/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation

struct ApiResponse: Codable {
    let userid: String?
    let token: String?
    let error: String?

    var isSuccess: Bool {
        return userid != nil && token != nil
    }

    var errorMessage: String? {
        return error
    }
}

