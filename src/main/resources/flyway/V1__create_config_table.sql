CREATE TABLE member_to_member_configuration(
    institution_id varchar(36) PRIMARY KEY NOT NULL,
    loans_as_from boolean NOT NULL DEFAULT FALSE,
    secondary_accounts_as_from boolean NOT NULL DEFAULT FALSE,
);
