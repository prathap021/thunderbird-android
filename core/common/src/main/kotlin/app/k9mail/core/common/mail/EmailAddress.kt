package app.k9mail.core.common.mail

/**
 * Represents an email address.
 *
 * This class currently doesn't support internationalized domain names (RFC 5891) or non-ASCII local parts (RFC 6532).
 */
class EmailAddress internal constructor(
    val localPart: String,
    val domain: EmailDomain,
) {
    val encodedLocalPart: String = if (localPart.isDotString) localPart else quoteString(localPart)

    val warnings: Set<Warning>

    init {
        warnings = buildSet {
            if (localPart.isEmpty()) {
                add(Warning.EmptyLocalPart)
            }

            if (!localPart.isDotString) {
                add(Warning.QuotedStringInLocalPart)
            }
        }
    }

    val address: String
        get() = "$encodedLocalPart@$domain"

    val normalizedAddress: String
        get() = "$encodedLocalPart@${domain.normalized}"

    override fun toString(): String {
        return address
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EmailAddress

        if (localPart != other.localPart) return false
        return domain == other.domain
    }

    override fun hashCode(): Int {
        var result = localPart.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }

    private fun quoteString(input: String): String {
        return buildString {
            append(DQUOTE)
            for (character in input) {
                if (!character.isQtext) {
                    append(BACKSLASH)
                }
                append(character)
            }
            append(DQUOTE)
        }
    }

    enum class Warning {
        /**
         * The local part requires using a quoted string.
         *
         * This is valid, but very uncommon. Using such a local part should be avoided whenever possible.
         */
        QuotedStringInLocalPart,

        /**
         * The local part is the empty string.
         *
         * Even if you want to allow quoted strings, you probably don't want to allow this.
         */
        EmptyLocalPart,
    }

    companion object {
        fun parse(address: String, config: EmailAddressParserConfig = EmailAddressParserConfig.DEFAULT): EmailAddress {
            return EmailAddressParser(address, config).parse()
        }
    }
}

fun String.toEmailAddress() = EmailAddress.parse(this)
