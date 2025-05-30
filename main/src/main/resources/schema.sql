drop table if exists "advertisement" cascade;
drop table if exists "apply" cascade;
drop table if exists "comment" cascade;
drop table if exists "like" cascade;
drop table if exists "tag" cascade;
drop table if exists "flash" cascade;
drop table if exists "post" cascade;
drop table if exists "meeting" cascade;
drop table if exists "notice" cascade;
drop table if exists "report" cascade;
drop table if exists "user" cascade;
drop table if exists "co_leader" cascade;

DROP TYPE IF EXISTS meeting_joinableparts_enum;

create type meeting_joinableparts_enum as enum ('PM', 'DESIGN', 'IOS', 'ANDROID', 'SERVER', 'WEB');

create table if not exists "user"
(
    id             serial
    constraint "PK_cace4a159ff9f2512dd42373760"
    primary key
    constraint "UQ_cace4a159ff9f2512dd42373760"
    unique,
    name           varchar not null,
    "profileImage" varchar,
    activities     jsonb,
    phone          varchar,
    "interestedKeywords" jsonb,
    "isAlarmed"    boolean default false,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists meeting
(
    id                            serial
    constraint "PK_dccaf9e4c0e39067d82ccc7bb83"
    primary key
    constraint "UQ_dccaf9e4c0e39067d82ccc7bb83"
    unique,
    "userId"                      integer   not null
    constraint "FK_854982a74818bb6307419e0e6b8"
    references "user"
    on delete cascade,
    title                         varchar   not null,
    category                      varchar   not null,
    "imageURL"                    jsonb     not null,
    "startDate"                   timestamp not null,
    "endDate"                     timestamp not null,
    capacity                      integer   not null,
    "desc"                        varchar   not null,
    "processDesc"                 varchar   not null,
    "mStartDate"                  timestamp not null,
    "mEndDate"                    timestamp not null,
    "leaderDesc"                  varchar,
    note                          varchar,
    "isMentorNeeded"              boolean   not null,
    "canJoinOnlyActiveGeneration" boolean   not null,
    "targetActiveGeneration"      integer,
    "joinableParts"               meeting_joinableparts_enum[],
    "createdGeneration"           integer   default 32,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists co_leader
(
    id          serial
    primary key,
    "meetingId" integer not null
    constraint fk_meeting
    references meeting
    on delete cascade,
    "userId"    integer not null
    constraint fk_user
    references "user"
    on delete cascade,
    "createdTimestamp"  timestamp default CURRENT_TIMESTAMP not null,
    "modifiedTimestamp" timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists flash
(
    id                  serial
    primary key,
    "leaderUserId"      integer                                        not null,
    title               varchar(30)                                    not null,
    "desc"              text,
    "activityStartDate" timestamp                                      not null,
    "activityEndDate"   timestamp                                      not null,
    "flashPlace"        varchar(255),
    "minimumCapacity"   integer                                        not null,
    "maximumCapacity"   integer                                        not null,
    "imageURL"          jsonb,
    "createdTimestamp"  timestamp default CURRENT_TIMESTAMP            not null,
    "modifiedTimestamp" timestamp default CURRENT_TIMESTAMP            not null,
    "createdGeneration" integer                                        not null,
    "flashTimingType"   varchar,
    "flashPlaceType"    varchar,
    "startDate"         timestamp                                      not null,
    "endDate"           timestamp                                      not null,
    "meetingId"         integer
    );

create table if not exists tag
(
    id                     serial
    primary key,
    "tagType"              varchar                        not null,
    "meetingId"            integer,
    "flashId"              integer,
    "welcomeMessageTypes"  jsonb,
    "meetingKeywordTypes"  jsonb,
    "createdTimestamp"     timestamp default CURRENT_TIMESTAMP not null,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists apply
(
    id            serial
    constraint "PK_c61ed680472aa0f58499175d902"
    primary key,
    type          integer   default 0 not null,
    "meetingId"   integer             not null
    constraint "FK_b130d23f4642d1ef51c6e54d257"
    references meeting
    on delete cascade,
    "userId"      integer             not null
    constraint "FK_359c8244808809db5ee96ed066e"
    references "user"
    on delete cascade,
    content       varchar                     ,
    "appliedDate" timestamp           not null,
    status        integer   default 0 not null,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create index if not exists "meetingId_index"
    on apply ("meetingId");

create index if not exists "userId_index"
    on apply ("userId");

create table if not exists notice
(
    id                serial
    constraint "PK_705062b14410ff1a04998f86d72"
    primary key
    constraint "UQ_705062b14410ff1a04998f86d72"
    unique,
    title             varchar   not null,
    "subTitle"        varchar   not null,
    contents          varchar   not null,
    "createdDate"     timestamp not null,
    "exposeStartDate" timestamp not null,
    "exposeEndDate"   timestamp not null,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists post
(
    id             serial
    constraint "PK_be5fda3aac270b134ff9c21cdee"
    primary key
    constraint "UQ_be5fda3aac270b134ff9c21cdee"
    unique,
    contents       varchar             not null,
    "createdDate"  timestamp           not null,
    "updatedDate"  timestamp           not null,
    "likeCount"    integer   default 0 not null,
    "userId"       integer             not null
    constraint "FK_5c1cf55c308037b5aca1038a131"
    references "user",
    "meetingId"    integer             not null
    constraint "FK_85e980cf9166f5337c0b2b76bc0"
    references meeting,
    title          varchar             not null,
    "viewCount"    integer   default 0 not null,
    images         text[],
    "commentCount" integer   default 0 not null,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists comment
(
    id            serial
    constraint "PK_0b0e4bbc8415ec426f87f3a88e2"
    primary key
    constraint "UQ_0b0e4bbc8415ec426f87f3a88e2"
    unique,
    contents      varchar             not null,
    depth         integer   default 0 not null,
    "order"       integer   default 0 not null,
    "createdDate" timestamp           not null,
    "updatedDate" timestamp           not null,
    "likeCount"   integer   default 0 not null,
    "userId"      integer
    constraint "FK_c0354a9a009d3bb45a08655ce3b"
    references "user",
    "postId"      integer             not null
    constraint "FK_94a85bb16d24033a2afdd5df060"
    references post
    on delete cascade,
    "parentId"    integer,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists "like"
(
    id            serial
    constraint "PK_eff3e46d24d416b52a7e0ae4159"
    primary key
    constraint "UQ_eff3e46d24d416b52a7e0ae4159"
    unique,
    "createdDate" timestamp not null,
    "userId"      integer   not null
    constraint "FK_e8fb739f08d47955a39850fac23"
    references "user",
    "postId"      integer
    constraint "FK_3acf7c55c319c4000e8056c1279"
    references post
    on delete cascade,
    "commentId"   integer
    constraint "FK_d86e0a3eeecc21faa0da415a18a"
    references comment
    on delete cascade,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists report
(
    id            serial
    constraint "PK_99e4d0bea58cba73c57f935a546"
    primary key
    constraint "UQ_99e4d0bea58cba73c57f935a546"
    unique,
    "createdDate" timestamp not null,
    "commentId"   integer
    constraint "FK_97372830f2390803a3e2df4a46e"
    references comment,
    "userId"      integer   not null
    constraint "FK_e347c56b008c2057c9887e230aa"
    references "user"
    on delete cascade,
    "postId"      integer
    constraint "FK_4b6fe2df37305bc075a4a16d3ea"
    references post
    on delete cascade,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
);

create table if not exists advertisement
(
    id                             serial
    primary key,
    "advertisementLink"            varchar(255),
    "advertisementEndDate"         timestamp(6),
    "advertisementStartDate"       timestamp(6),
    priority                       bigint,
    "advertisementCategory"        varchar(255)
    constraint "advertisement_advertisementCategory_check"
    check (("advertisementCategory")::text = ANY
((ARRAY ['POST'::character varying, 'MEETING'::character varying])::text[])),
    "advertisementDesktopImageUrl" varchar(255),
    "advertisementMobileImageUrl"  varchar(255),
    "isSponsoredContent"           boolean   default false not null,
    "createdTimestamp"    timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp"    timestamp default CURRENT_TIMESTAMP
    );
