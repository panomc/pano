package com.panomc.platform

// last ID 128
enum class ErrorCode {
    INVALID_DATA,

    REGISTER_USERNAME_EMPTY,
    REGISTER_EMAIL_EMPTY,
    REGISTER_PASSWORD_EMPTY,

    REGISTER_USERNAME_TOO_SHORT,
    REGISTER_USERNAME_TOO_LONG,

    REGISTER_PASSWORD_TOO_SHORT,
    REGISTER_PASSWORD_TOO_LONG,

    REGISTER_INVALID_USERNAME,
    REGISTER_INVALID_EMAIL,

    REGISTER_CANT_VERIFY_ROBOT,

    REGISTER_EMAIL_AND_EMAIL_REPEAT_NOT_SAME,
    REGISTER_PASSWORD_AND_PASSWORD_REPEAT_NOT_SAME,

    REGISTER_EMAIL_NOT_AVAILABLE,

    FINISH_API_CANT_CONNECT_DATABASE_PLEASE_CHECK_YOUR_INFO,
    FINISH_API_SOMETHING_WENT_WRONG_IN_DATABASE,

    LOGIN_EMAIL_EMPTY,
    LOGIN_PASSWORD_EMPTY,

    LOGIN_INVALID_EMAIL,
    LOGIN_INVALID_PASSWORD,

    LOGIN_CANT_VERIFY_ROBOT,

    LOGIN_WRONG_EMAIL_OR_PASSWORD,

    CANT_CONNECT_DATABASE,

    CONNECT_NEW_SERVER_API_PLATFORM_CODE_WRONG,

    PAGE_NOT_FOUND,

    NOT_EXISTS,

    POST_NOT_FOUND,
    UNKNOWN,

    UNKNOWN_ERROR_1,
    UNKNOWN_ERROR_2,
    UNKNOWN_ERROR_3,
    UNKNOWN_ERROR_7,
    UNKNOWN_ERROR_8,

    UNKNOWN_ERROR_12,
    UNKNOWN_ERROR_13,
    UNKNOWN_ERROR_14,
    UNKNOWN_ERROR_15,
    UNKNOWN_ERROR_16,
    UNKNOWN_ERROR_17,
    UNKNOWN_ERROR_18,
    UNKNOWN_ERROR_19,
    UNKNOWN_ERROR_20,

    UNKNOWN_ERROR_22,
    UNKNOWN_ERROR_23,
    UNKNOWN_ERROR_24,
    UNKNOWN_ERROR_25,
    UNKNOWN_ERROR_26,
    UNKNOWN_ERROR_27,
    UNKNOWN_ERROR_28,
    UNKNOWN_ERROR_29,

    UNKNOWN_ERROR_65,
    UNKNOWN_ERROR_66,
    UNKNOWN_ERROR_67,
    UNKNOWN_ERROR_68,
    UNKNOWN_ERROR_69,
    UNKNOWN_ERROR_70,
    UNKNOWN_ERROR_71,
    UNKNOWN_ERROR_72,
    UNKNOWN_ERROR_73,
    UNKNOWN_ERROR_74,
    UNKNOWN_ERROR_76,
    UNKNOWN_ERROR_78,
    UNKNOWN_ERROR_79,
    UNKNOWN_ERROR_80,
    UNKNOWN_ERROR_82,
    UNKNOWN_ERROR_84,
    UNKNOWN_ERROR_85,
    UNKNOWN_ERROR_86,
    UNKNOWN_ERROR_87,
    UNKNOWN_ERROR_88,
    UNKNOWN_ERROR_89,
    UNKNOWN_ERROR_90,
    UNKNOWN_ERROR_91,
    UNKNOWN_ERROR_92,
    UNKNOWN_ERROR_93,
    UNKNOWN_ERROR_94,
    UNKNOWN_ERROR_95,
    UNKNOWN_ERROR_96,
    UNKNOWN_ERROR_97,
    UNKNOWN_ERROR_98,
    UNKNOWN_ERROR_99,
    UNKNOWN_ERROR_100,
    UNKNOWN_ERROR_101,
    UNKNOWN_ERROR_102,
    UNKNOWN_ERROR_103,
    UNKNOWN_ERROR_104,
    UNKNOWN_ERROR_105,
    UNKNOWN_ERROR_106,
    UNKNOWN_ERROR_107,
    UNKNOWN_ERROR_108,
    UNKNOWN_ERROR_109,
    UNKNOWN_ERROR_110,
    UNKNOWN_ERROR_111,
    UNKNOWN_ERROR_112,
    UNKNOWN_ERROR_113,
    UNKNOWN_ERROR_114,
    UNKNOWN_ERROR_115,
    UNKNOWN_ERROR_116,
    UNKNOWN_ERROR_117,
    UNKNOWN_ERROR_118,
    UNKNOWN_ERROR_121,
    UNKNOWN_ERROR_124,
    UNKNOWN_ERROR_125,
    UNKNOWN_ERROR_128,
    UNKNOWN_ERROR_129,
    UNKNOWN_ERROR_130,
    UNKNOWN_ERROR_131,
    UNKNOWN_ERROR_132,
    UNKNOWN_ERROR_133,
    UNKNOWN_ERROR_134,
    UNKNOWN_ERROR_135,
    UNKNOWN_ERROR_136,
    UNKNOWN_ERROR_137,
    UNKNOWN_ERROR_138,
    UNKNOWN_ERROR_139,
    UNKNOWN_ERROR_140,
    UNKNOWN_ERROR_141,
    UNKNOWN_ERROR_142,
    UNKNOWN_ERROR_143,
    UNKNOWN_ERROR_144,
    UNKNOWN_ERROR_145,
    UNKNOWN_ERROR_146,
    UNKNOWN_ERROR_147,
    UNKNOWN_ERROR_148,
    UNKNOWN_ERROR_149,
    UNKNOWN_ERROR_150,
    UNKNOWN_ERROR_151,
    UNKNOWN_ERROR_152,
    UNKNOWN_ERROR_153,
    UNKNOWN_ERROR_154,
    UNKNOWN_ERROR_155,
    UNKNOWN_ERROR_156,
    UNKNOWN_ERROR_157,
    UNKNOWN_ERROR_158,
    UNKNOWN_ERROR_159,
    UNKNOWN_ERROR_160,
    UNKNOWN_ERROR_161,
    UNKNOWN_ERROR_162,
    UNKNOWN_ERROR_163,
    UNKNOWN_ERROR_164,
    UNKNOWN_ERROR_165,
    UNKNOWN_ERROR_166,
    UNKNOWN_ERROR_167,
    UNKNOWN_ERROR_168,
    UNKNOWN_ERROR_169,
    UNKNOWN_ERROR_170,
    UNKNOWN_ERROR_171,
    UNKNOWN_ERROR_172,
    UNKNOWN_ERROR_173,
    UNKNOWN_ERROR_174,
    UNKNOWN_ERROR_175,
    UNKNOWN_ERROR_176,
    UNKNOWN_ERROR_177,
    UNKNOWN_ERROR_178,
    UNKNOWN_ERROR_179,
    UNKNOWN_ERROR_180,
    UNKNOWN_ERROR_181,
    UNKNOWN_ERROR_182,
    UNKNOWN_ERROR_183,
    UNKNOWN_ERROR_184,
    UNKNOWN_ERROR_185,
    UNKNOWN_ERROR_186,
    UNKNOWN_ERROR_187,
    UNKNOWN_ERROR_188,
    UNKNOWN_ERROR_189,
    UNKNOWN_ERROR_190,
    UNKNOWN_ERROR_191,
    UNKNOWN_ERROR_192,
    UNKNOWN_ERROR_193,
    UNKNOWN_ERROR_194,
}