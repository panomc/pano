const LANGUAGE = 'language'

const LanguageUtil = {
  getLanguage() {
    return localStorage.getItem(LANGUAGE)
  },

  saveLanguage(language) {
    localStorage.setItem(LANGUAGE, language)
  },

  isThereLanguage() {
    return !!LanguageUtil.getLanguage()
  }
}