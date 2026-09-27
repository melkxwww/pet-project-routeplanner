-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    public_id       UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    email           TEXT UNIQUE NOT NULL,
    password_hash   TEXT NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMPTZ
);

CREATE INDEX idx_users_public_id ON users(public_id);
CREATE INDEX idx_users_email ON users(email);

-- ============================================================
-- API KEYS (public_id нужен — юзер видит ключ в UI)
-- ============================================================
CREATE TABLE api_keys (
    id              BIGSERIAL PRIMARY KEY,
    public_id       UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    key_hash        TEXT NOT NULL,
    key_prefix      TEXT NOT NULL,
    name            TEXT,
    quota_per_day   INT NOT NULL DEFAULT 100,
    used_today      INT NOT NULL DEFAULT 0,
    quota_reset_at  DATE NOT NULL DEFAULT CURRENT_DATE,
    last_used_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at      TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_user ON api_keys(user_id) WHERE revoked_at IS NULL;
CREATE INDEX idx_api_keys_prefix ON api_keys(key_prefix);

-- ============================================================
-- USER PREFERENCES
-- ============================================================
CREATE TABLE user_preferences (
    user_id              BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    default_profile      TEXT,
    default_params       JSONB,
    home_location        GEOGRAPHY(POINT, 4326),
    preferred_units      TEXT DEFAULT 'metric',
    language             TEXT DEFAULT 'ru',
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ROUTES (public_id — то, что в URL)
-- ============================================================
CREATE TABLE routes (
    id              BIGSERIAL PRIMARY KEY,
    public_id       UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            TEXT,
    description     TEXT,

    geometry        GEOGRAPHY(LINESTRING, 4326) NOT NULL,

    distance_m      INT NOT NULL,
    duration_s      INT NOT NULL,
    elevation_gain_m INT,
    elevation_loss_m INT,

    params          JSONB NOT NULL,

    score           REAL,
    breakdown       JSONB,

    profile         TEXT NOT NULL,
    tags_version    INT NOT NULL,
    graph_version   TEXT NOT NULL,

    is_public       BOOLEAN NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routes_public_id ON routes(public_id);
CREATE INDEX idx_routes_user ON routes(user_id, created_at DESC);
CREATE INDEX idx_routes_geometry ON routes USING GIST(geometry);
CREATE INDEX idx_routes_public ON routes(is_public) WHERE is_public = TRUE;
CREATE INDEX idx_routes_profile ON routes(profile);

-- ============================================================
-- ROUTE SEGMENTS (внутренняя, public_id не нужен)
-- ============================================================
CREATE TABLE route_segments (
    id              BIGSERIAL PRIMARY KEY,
    route_id        BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    seq             INT NOT NULL,
    osm_way_id      BIGINT NOT NULL,
    distance_m      INT NOT NULL,

    shadiness       REAL,
    traffic_stress  REAL,
    surface_type    TEXT,

    UNIQUE (route_id, seq)
);

CREATE INDEX idx_route_segments_route ON route_segments(route_id);
CREATE INDEX idx_route_segments_way ON route_segments(osm_way_id);

-- ============================================================
-- ROUTE SHARES (share_token — уже публичный, public_id не нужен)
-- ============================================================
CREATE TABLE route_shares (
    id              BIGSERIAL PRIMARY KEY,
    route_id        BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    share_token     TEXT UNIQUE NOT NULL,
    expires_at      TIMESTAMPTZ,
    view_count      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_route_shares_token ON route_shares(share_token);
CREATE INDEX idx_route_shares_route ON route_shares(route_id);

-- ============================================================
-- WAY TAGS (osm_way_id — внешний ID, ок)
-- ============================================================
CREATE TABLE way_tags (
    osm_way_id      BIGINT PRIMARY KEY,

    shadiness       REAL CHECK (shadiness BETWEEN 0 AND 1),
    illumination    REAL CHECK (illumination BETWEEN 0 AND 1),
    traffic_stress  REAL CHECK (traffic_stress BETWEEN 0 AND 1),
    road_quality    REAL CHECK (road_quality BETWEEN 0 AND 1),
    surface_type    TEXT CHECK (surface_type IN
                       ('asphalt','paved','gravel','dirt','sand','unknown')),
    picturesqueness REAL CHECK (picturesqueness BETWEEN 0 AND 1),

    tag_confidence  REAL CHECK (tag_confidence BETWEEN 0 AND 1),

    tags_version    INT NOT NULL,
    source          TEXT NOT NULL DEFAULT 'baker',
    computed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_way_tags_version ON way_tags(tags_version);
CREATE INDEX idx_way_tags_surface ON way_tags(surface_type);
CREATE INDEX idx_way_tags_shade ON way_tags(shadiness) WHERE shadiness > 0.5;
CREATE INDEX idx_way_tags_stress ON way_tags(traffic_stress) WHERE traffic_stress < 0.3;

-- ============================================================
-- USER EVENTS (внутренняя)
-- ============================================================
CREATE TABLE user_events (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    event_type      TEXT NOT NULL,
    route_id        BIGINT REFERENCES routes(id) ON DELETE SET NULL,

    params          JSONB,
    metadata        JSONB,

    ip_address      INET,
    user_agent      TEXT,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_events_user_time ON user_events(user_id, created_at DESC);
CREATE INDEX idx_events_type_time ON user_events(event_type, created_at DESC);
CREATE INDEX idx_events_route ON user_events(route_id) WHERE route_id IS NOT NULL;

-- ============================================================
-- USER SEGMENT HISTORY
-- ============================================================
CREATE TABLE user_segment_history (
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    osm_way_id      BIGINT NOT NULL,
    visit_count     INT NOT NULL DEFAULT 1,
    first_visit     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_visit      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, osm_way_id)
);

CREATE INDEX idx_segment_history_user ON user_segment_history(user_id);
CREATE INDEX idx_segment_history_way ON user_segment_history(osm_way_id);
CREATE INDEX idx_segment_history_recent
    ON user_segment_history(user_id, last_visit DESC);

-- ============================================================
-- FEEDBACK
-- ============================================================
CREATE TABLE feedback (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    route_id        BIGINT REFERENCES routes(id) ON DELETE CASCADE,
    osm_way_id      BIGINT,

    type            TEXT NOT NULL,
    category        TEXT,
    comment         TEXT,

    location        GEOGRAPHY(POINT, 4326),

    status          TEXT NOT NULL DEFAULT 'pending',

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at     TIMESTAMPTZ,
    reviewed_by     BIGINT REFERENCES users(id)
);

CREATE INDEX idx_feedback_route ON feedback(route_id) WHERE route_id IS NOT NULL;
CREATE INDEX idx_feedback_way ON feedback(osm_way_id) WHERE osm_way_id IS NOT NULL;
CREATE INDEX idx_feedback_status ON feedback(status, created_at DESC);
CREATE INDEX idx_feedback_user ON feedback(user_id, created_at DESC);

-- ============================================================
-- CORRECTIONS
-- ============================================================
CREATE TABLE corrections (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    osm_way_id      BIGINT NOT NULL,

    tag_name        TEXT NOT NULL,
    current_value   TEXT,
    suggested_value TEXT NOT NULL,
    reason          TEXT,
    comment         TEXT,

    confidence      REAL CHECK (confidence BETWEEN 0 AND 1),

    status          TEXT NOT NULL DEFAULT 'pending',
    reviewed_by     BIGINT REFERENCES users(id),
    reviewed_at     TIMESTAMPTZ,
    applied_at      TIMESTAMPTZ,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_corrections_way ON corrections(osm_way_id);
CREATE INDEX idx_corrections_status ON corrections(status, created_at DESC);
CREATE INDEX idx_corrections_user ON corrections(user_id);

-- ============================================================
-- REGIONS (id — текстовый код, ок)
-- ============================================================
CREATE TABLE regions (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    bbox            GEOGRAPHY(POLYGON, 4326),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- TAG VERSIONS
-- ============================================================
CREATE TABLE tag_versions (
    version         INT PRIMARY KEY,
    description     TEXT,
    schema_json     JSONB NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- GRAPH VERSIONS (id — текстовый, типа 'crimea-2026-09-20')
-- ============================================================
CREATE TABLE graph_versions (
    id              TEXT PRIMARY KEY,
    region_id       TEXT NOT NULL REFERENCES regions(id),
    tags_version    INT NOT NULL REFERENCES tag_versions(version),
    pbf_file        TEXT NOT NULL,
    imported_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_current      BOOLEAN NOT NULL DEFAULT FALSE,
    notes           TEXT
);

CREATE UNIQUE INDEX idx_graph_current_per_region
    ON graph_versions(region_id) WHERE is_current = TRUE;

-- ============================================================
-- WEATHER CACHE
-- ============================================================
CREATE TABLE weather_cache (
    id              BIGSERIAL PRIMARY KEY,
    lat_rounded     NUMERIC(5,2) NOT NULL,
    lon_rounded     NUMERIC(5,2) NOT NULL,
    forecast_time   TIMESTAMPTZ NOT NULL,

    temperature_c   REAL,
    wind_speed_ms   REAL,
    wind_direction  INT,
    precipitation_mm REAL,
    cloud_cover     INT,

    source          TEXT NOT NULL DEFAULT 'open-meteo',
    fetched_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (lat_rounded, lon_rounded, forecast_time)
);

CREATE INDEX idx_weather_lookup
    ON weather_cache(lat_rounded, lon_rounded, forecast_time);
CREATE INDEX idx_weather_cleanup ON weather_cache(fetched_at);

-- ============================================================
-- GENERATION LOG
-- ============================================================
CREATE TABLE generation_log (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    request_id      UUID NOT NULL,

    query_text      TEXT,
    params          JSONB,

    candidates_count INT,
    scored_count    INT,
    top_score       REAL,

    parse_ms        INT,
    generate_ms     INT,
    score_ms        INT,
    total_ms        INT,

    error           TEXT,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gen_log_user ON generation_log(user_id, created_at DESC);
CREATE INDEX idx_gen_log_time ON generation_log(created_at DESC);
CREATE INDEX idx_gen_log_errors ON generation_log(created_at DESC)
    WHERE error IS NOT NULL;