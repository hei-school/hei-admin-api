update "user" u set specialization_field = 'COMMON_CORE'
    where u.specialization_field is null;