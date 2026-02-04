DO
$$
    BEGIN
        ALTER TYPE advanced_fee_stats_type
            ADD VALUE 'UNPAID_COUNT';
        EXCEPTION
            WHEN duplicate_object THEN NULL;
    END
    $$;
