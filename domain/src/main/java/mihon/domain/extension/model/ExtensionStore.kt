package mihon.domain.extension.model

data class ExtensionStore(
    val indexUrl: String,
    val name: String,
    val badgeLabel: String,
    val signingKey: String,
    val contact: Contact,
    val isLegacy: Boolean,
    val extensionListUrl: String?,
) {
    data class Contact(
        val website: String,
        val discord: String?,
    )
}

const val REPO_HELP = "https://github.com/the-nexus-app/NEXUS"

// cuong-tran's key
const val KOMIKKU_SIGNATURE = "cbec121aa82ebb02aaa73806992e0368a97d47b5451ed6524816d03084c45905"
const val REPO_SIGNATURE = "9add655a78e96c4ec7a53ef89dccb557cb5d767489fac5e785d671a5a75d4da2"

// NXS --> NEXUS signing key (SHA-256 of the certificate in nexus-release-key.jks)
const val NEXUS_SIGNATURE = "f3d624332ef08b5d7a40710ab6b7bb41779f86dbdcf9b8f876077bb76bea1eb3"
// NXS <--
