package com.example.core.constants

object AppConstants {
  const val ASHRAM_NAME = "PARAMDHAM PODHI ASHRAM"
  const val MAIN_ADMIN = "Mahadev Pranami"
  const val CONSULTATION_NUMBER = "9654053044"

  // App Languages
  enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी (Hindi)"),
    GUJARATI("gu", "ગુજરાતી (Gujarati)")
  }

  // Curated Divine Thoughts
  val DIVINE_THOUGHTS = mapOf(
    AppLanguage.ENGLISH to listOf(
      "The soul is eternal, peaceful, and filled with infinite light. Turn inward to experience it.",
      "Surrender your worries to Raj Shyam Ji Maharaj and let His divine grace guide your steps.",
      "Service to humanity with a pure heart is the highest form of worship.",
      "Divine love is not a transaction; it is the ultimate state of consciousness.",
      "Quiet the mind, and the voice of the soul will speak."
    ),
    AppLanguage.HINDI to listOf(
      "आत्मा शाश्वत, शांत और अनंत प्रकाश से भरी है। इसका अनुभव करने के लिए भीतर मुड़ें।",
      "अपनी चिंताओं को राज श्याम जी महाराज को सौंप दें और उनकी दिव्य कृपा को अपना मार्गदर्शक बनने दें।",
      "शुद्ध हृदय से मानवता की सेवा ही पूजा का सर्वोच्च रूप है।",
      "दिव्य प्रेम कोई सौदा नहीं है; यह चेतना की अंतिम अवस्था है।",
      "मन को शांत करो, और आत्मा की आवाज सुनाई देगी।"
    ),
    AppLanguage.GUJARATI to listOf(
      "આત્મા શાશ્વત, શાંત અને અનંત પ્રકાશથી ભરેલો છે. તેનો અનુભવ કરવા માટે ભીતર વળો.",
      "તમારી ચિંતાઓને રાજ શ્યામજી મહારાજને સોંપી દો અને તેમની દિવ્ય કૃપાને તમારા માર્ગદર્શક બનવા દો.",
      "શુદ્ધ હૃદયથી માનવતાની સેવા એ જ પૂજાનું સર્વોચ્ચ સ્વરૂપ છે.",
      "દિવ्य પ્રેમ એ કોઈ સોદો નથી; તે ચેતનાની પરમ અવસ્થા છે.",
      "મનને શાંત કરો, અને આત્માનો અવાજ સંભળાશે."
    )
  )

  // Today's Holy Vani
  data class HolyVani(
    val id: String,
    val title: String,
    val verse: String,
    val translation: String,
    val chapter: String
  )

  val HOLY_VANIS = mapOf(
    AppLanguage.ENGLISH to listOf(
      HolyVani(
        "1",
        "Siddhant Vani - Verse 12",
        "अक्षर अतीत पार है, ताके पार प्राननाथ।\nताकी सेवा तुम करो, सदा राखो सिर हाथ॥",
        "The Supreme Lord is beyond the indestructible cosmos. Always worship Him and feel His protective hand over your head.",
        "Chapter 1: Divine Union"
      ),
      HolyVani(
        "2",
        "Sunderkand - Chopai 4",
        "कवन सो काज कठिन जग माहीं।\nजो नहिं होइ तात तुम पाहीं॥",
        "What task is too difficult in this world that cannot be accomplished by your devotion and grace?",
        "Chapter 3: Strength of Devotion"
      )
    ),
    AppLanguage.HINDI to listOf(
      HolyVani(
        "1",
        "सिद्धांत वाणी - श्लोक 12",
        "अक्षर अतीत पार है, ताके पार प्राननाथ।\nताकी सेवा तुम करो, सदा राखो सिर हाथ॥",
        "अक्षर पुरुष से भी परे परमधाम के स्वामी श्री प्राणनाथ जी हैं। उन्हीं की सेवा करो और सदा उनका वरदहस्त अपने शीश पर महसूस करो।",
        "अध्याय 1: दिव्य मिलन"
      ),
      HolyVani(
        "2",
        "सुंदरकांड - चौपाई 4",
        "कवन सो काज कठिन जग माहीं।\nजो नहिं होइ तात तुम पाहीं॥",
        "इस संसार में ऐसा कौन सा कठिन कार्य है जो आपकी भक्ति और कृपा से संभव न हो सके?",
        "अध्याय 3: भक्ति का बल"
      )
    ),
    AppLanguage.GUJARATI to listOf(
      HolyVani(
        "1",
        "સિદ્ધાંત વાણી - શ્લોક 12",
        "अक्षर अतीत पार है, ताके पार प्राननाथ।\nताकी सेवा तुम करो, सदा राखो सिर हाथ॥",
        "અક્ષર પુરુષથી પણ પર પરમધામના સ્વામી શ્રી પ્રાણનાથજી છે. તેમની જ સેવા કરો અને સદા તેમનો આશીર્વાદ આપના શીશ પર અનુભવો.",
        "પ્રકરણ 1: દિવ્ય મિલન"
      ),
      HolyVani(
        "2",
        "સુંદરકાંડ - ચોપાઈ 4",
        "कवन सो काज कठिन जग माहीं।\nजो नहिं होइ तात तुम पाहीं॥",
        "આ સંસારમાં એવું કયું મુશ્કેલ કાર્ય છે જે તમારી ભક્તિ અને કૃપાથી સંભવ ન થઈ શકે?",
        "પ્રકરણ 3: ભક્તિનું બળ"
      )
    )
  )

  // UI Strings Translation
  val TRANSLATIONS = mapOf(
    AppLanguage.ENGLISH to mapOf(
      "home" to "Home",
      "chats" to "Chats",
      "sabha" to "Live Sabha",
      "library" to "Library",
      "gallery" to "Gallery",
      "more" to "More",
      "welcome_user" to "Pranam, Seva Ji",
      "divine_thought" to "Today's Divine Thought",
      "holy_vani" to "Today's Holy Vani",
      "quick_join" to "Quick Join Sabha",
      "sabha_status_live" to "LIVE NOW",
      "sabha_status_upcoming" to "UPCOMING",
      "join_now" to "Join Now",
      "prayer_reminder" to "Daily Prayer Sadhana",
      "japa_counter" to "Japa Mala Counter",
      "meditation_timer" to "Meditation Sadhana (Mins)",
      "upcoming_programs" to "Upcoming Ashram Programs",
      "announcements" to "Latest Announcements",
      "about_admin" to "Main Administrator",
      "contact_us" to "Contact & Consultation",
      "settings" to "Settings",
      "theme" to "Theme Customization",
      "language" to "App Language",
      "notifications" to "Notifications",
      "about_ashram" to "About Paramdham Podhi Ashram",
      "privacy_policy" to "Privacy & Spiritual Code",
      "onboarding_skip" to "Skip",
      "onboarding_next" to "Next",
      "onboarding_start" to "Enter Ashram",
      "share_thought" to "Share Thought",
      "copied" to "Copied to clipboard!",
      "sadhana_saved" to "Sadhana progress updated!"
    ),
    AppLanguage.HINDI to mapOf(
      "home" to "होम",
      "chats" to "चैट्स",
      "sabha" to "लाइव सभा",
      "library" to "पुस्तकालय",
      "gallery" to "गैलरी",
      "more" to "अधिक",
      "welcome_user" to "प्रणाम, सेवा जी",
      "divine_thought" to "आज का दिव्य विचार",
      "holy_vani" to "आज की पवित्र वाणी",
      "quick_join" to "शीघ्र जुड़ें",
      "sabha_status_live" to "अभी लाइव है",
      "sabha_status_upcoming" to "आगामी कार्यक्रम",
      "join_now" to "अभी जुड़ें",
      "prayer_reminder" to "दैनिक प्रार्थना साधना",
      "japa_counter" to "जाप माला काउंटर",
      "meditation_timer" to "ध्यान साधना (मिनट)",
      "upcoming_programs" to "आगामी आश्रम कार्यक्रम",
      "announcements" to "नवीनतम घोषणाएं",
      "about_admin" to "मुख्य प्रशासक",
      "contact_us" to "संपर्क और परामर्श",
      "settings" to "सेटिंग्स",
      "theme" to "थीम कस्टमाइजेशन",
      "language" to "ऐप की भाषा",
      "notifications" to "अधिसूचनाएं",
      "about_ashram" to "परमधाम पोढ़ी आश्रम के बारे में",
      "privacy_policy" to "गोपनीयता और आध्यात्मिक संहिता",
      "onboarding_skip" to "छोड़ें",
      "onboarding_next" to "अगला",
      "onboarding_start" to "आश्रम में प्रवेश करें",
      "share_thought" to "विचार साझा करें",
      "copied" to "क्लिपबोर्ड पर कॉपी किया गया!",
      "sadhana_saved" to "साधना प्रगति अपडेट की गई!"
    ),
    AppLanguage.GUJARATI to mapOf(
      "home" to "હોમ",
      "chats" to "ચેટ્સ",
      "sabha" to "લાઇવ સભા",
      "library" to "પુસ્તકાલય",
      "gallery" to "ગેલેરી",
      "more" to "વધુ",
      "welcome_user" to "પ્રણામ, સેવા જી",
      "divine_thought" to "આજનો દિવ્ય વિચાર",
      "holy_vani" to "આજની પવિત્ર વાણી",
      "quick_join" to "ઝડપથી જોડાઓ",
      "sabha_status_live" to "અત્યારે લાઇવ છે",
      "sabha_status_upcoming" to "આગામી કાર્યક્રમ",
      "join_now" to "હમણાં જોડાઓ",
      "prayer_reminder" to "દૈનિક પ્રાર્થના સાધના",
      "japa_counter" to "જાપ માળા કાઉન્ટર",
      "meditation_timer" to "ધ્યાન સાધના (મિનિટ)",
      "upcoming_programs" to "આગામી આશ્રમ કાર્યક્રમો",
      "announcements" to "નવીનતમ જાહેરાતો",
      "about_admin" to "મુખ્ય પ્રશાસક",
      "contact_us" to "સંપર્ક અને પરામર્શ",
      "settings" to "સેટિંગ્સ",
      "theme" to "થીમ પસંદગી",
      "language" to "એપની ભાષા",
      "notifications" to "નોટિફિકેશન",
      "about_ashram" to "પરમધામ પોઢી આશ્રમ વિશે",
      "privacy_policy" to "ગોપનીયતા અને આધ્યાત્મિક સંહિતા",
      "onboarding_skip" to "છોડી દો",
      "onboarding_next" to "આગળ",
      "onboarding_start" to "આશ્રમમાં પ્રવેશ કરો",
      "share_thought" to "વિચાર શેર કરો",
      "copied" to "ક્લિપબોર્ડ પર કોપી કરવામાં આવ્યું!",
      "sadhana_saved" to "સાધના પ્રગતિ અપડેટ થઈ ગઈ!"
    )
  )

  fun getTranslation(lang: AppLanguage, key: String): String {
    return TRANSLATIONS[lang]?.get(key) ?: key
  }
}
