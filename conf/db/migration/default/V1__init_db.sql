DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'signalconso_antivirus') THEN
            CREATE SCHEMA signalconso_antivirus;
        END IF;
    END
$$;

create table if not exists signalconso_antivirus.file_data
(
    id               uuid                     not null primary key,
    external_id      varchar                  not null unique,
    creation_date    TIMESTAMP WITH TIME ZONE NOT NULL,
    filename         varchar                  NOT NULL unique,
    scan_result   numeric,
    av_output        varchar
);
