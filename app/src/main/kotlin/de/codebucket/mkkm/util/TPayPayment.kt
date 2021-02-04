package de.codebucket.mkkm.util

import android.net.Uri
import android.text.TextUtils

class TPayPayment {
    class Builder {

        companion object {
            private val TRANSLATIONS = mapOf(
                "amount" to "kwota",
                "description" to "opis",
                "return_url" to "pow_url",
                "return_error_url" to "pow_url_blad",
                "name" to "nazwisko"
            )
        }

        private val params: HashMap<String, String?> = HashMap()
        private val allowedFields = arrayOf(
            "id",
            "amount",
            "description",
            "crc",
            "md5sum",
            "online",
            "return_url",
            "return_error_url",
            "email",
            "name"
        )

        fun fromPaymentLink(url: String?): Builder {
            val data = Uri.parse(url)

            if (!data.host.equals("secure.transferuj.pl", true) && !data.host.equals("secure.tpay.com", true)) {
                throw IllegalArgumentException("URL is not a TPay payment link")
            }

            if (data.getQueryParameter("id") == null) {
                throw IllegalArgumentException("Merchant id cannot be null")
            }

            var useEnglishParams = true

            // Figure out which parameter names are being used, english or polish
            if (data.getQueryParameter("kwota") != null) {
                useEnglishParams = false
            }

            for (i in allowedFields.indices) {
                var field = allowedFields[i]
                if (!useEnglishParams && TRANSLATIONS.containsKey(field)) {
                    field = TRANSLATIONS.getValue(field)
                }

                if (!TextUtils.isEmpty(data.getQueryParameter(field))) {
                    params[allowedFields[i]] = data.getQueryParameter(field)
                }
            }

            return this
        }

        val id: String?
            get() = params["id"]

        fun setId(id: String?): Builder {
            params["id"] = id
            return this
        }

        val amount: String?
            get() = params["amount"]

        fun setAmount(amount: String?): Builder {
            params["amount"] = amount
            return this
        }

        val description: String?
            get() = params["description"]

        fun setDescription(description: String?): Builder {
            params["description"] = description
            return this
        }

        val crc: String?
            get() = params["crc"]

        fun setCrc(crc: String?): Builder {
            params["crc"] = crc
            return this
        }

        val md5Sum: String?
            get() = params["md5sum"]

        fun setMd5Sum(md5sum: String?): Builder {
            params["md5sum"] = md5sum
            return this
        }

        val online: String?
            get() = params["online"]

        fun setOnline(online: String?): Builder {
            params["online"] = online
            return this
        }

        val returnUrl: String?
            get() = params["return_url"]

        fun setReturnUrl(returnUrl: String?): Builder {
            params["return_url"] = returnUrl
            return this
        }

        val returnErrorUrl: String?
            get() = params["return_error_url"]

        fun setReturnErrorUrl(returnErrorUrl: String?): Builder {
            params["return_error_url"] = returnErrorUrl
            return this
        }

        val clientEmail: String?
            get() = params["email"]

        fun setClientEmail(email: String?): Builder {
            params["email"] = email
            return this
        }

        val clientName: String?
            get() = params["name"]

        fun setClientName(name: String?): Builder {
            params["name"] = name
            return this
        }

        fun build(): Uri {
            val uriBuilder = Uri.Builder()
                .scheme("https")
                .encodedAuthority("secure.tpay.com")
                .encodedPath("/")

            for (field in allowedFields) {
                if (!TextUtils.isEmpty(params[field])) {
                    uriBuilder.appendQueryParameter(field, params[field])
                }
            }

            return uriBuilder.build()
        }
    }
}
