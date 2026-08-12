import { FormEvent, type ReactNode, useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  ArrowLeft,
  Bot,
  Check,
  Gauge,
  Home,
  Mail,
  RefreshCw,
  Search,
  ShieldCheck,
  Sparkles,
  TriangleAlert,
  UserPlus,
  Users,
} from "lucide-react";
import "./styles.css";

const API = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";
type Tweet = {
  id: string;
  authorId: string;
  content: string;
  createdAt: string;
};
type DiscoverUser = {
  id: string;
  username: string;
  displayName: string;
  type: string;
  createdAt?: string;
  avatarUrl?: string;
};
type MetricPoint = {
  timestamp: string;
  tweetsPerMinute: number;
  dmsPerMinute: number;
};
type Metrics = {
  agents: number;
  activeAgents: number;
  errors: number;
  health: string;
  points: MetricPoint[];
};
type Request = (path: string, options?: RequestInit) => Promise<unknown>;

function MetricCard({
  icon,
  label,
  value,
  detail,
  tone = "blue",
}: {
  icon: ReactNode;
  label: string;
  value: number | string;
  detail: string;
  tone?: "blue" | "purple" | "green" | "red";
}) {
  return (
    <section className={`metric-card ${tone}`}>
      <div className="metric-card-top">
        <span className="metric-icon">{icon}</span>
        <span>{label}</span>
      </div>
      <strong>{value}</strong>
      <small>{detail}</small>
    </section>
  );
}

function MetricGraph({
  label,
  points,
  color,
  value,
}: {
  label: string;
  points: MetricPoint[];
  color: string;
  value: (point: MetricPoint) => number;
}) {
  const width = 720;
  const height = 220;
  const padding = { top: 18, right: 18, bottom: 30, left: 36 };
  const values = points.map(value);
  const maximum = Math.max(1, ...values);
  const x = (index: number) =>
    padding.left +
    (index / Math.max(1, points.length - 1)) *
      (width - padding.left - padding.right);
  const y = (pointValue: number) =>
    height -
    padding.bottom -
    (pointValue / maximum) * (height - padding.top - padding.bottom);
  const line = points
    .map(
      (point, index) =>
        `${index ? "L" : "M"} ${x(index).toFixed(1)} ${y(value(point)).toFixed(1)}`,
    )
    .join(" ");
  const area = points.length
    ? `${line} L ${x(points.length - 1).toFixed(1)} ${height - padding.bottom} L ${x(0).toFixed(1)} ${height - padding.bottom} Z`
    : "";
  return (
    <section
      className="metric-graph"
      aria-label={`${label} over the last 60 minutes`}
    >
      <div className="graph-heading">
        <div>
          <span className="eyebrow">LAST 60 MINUTES</span>
          <h2>{label}</h2>
        </div>
        <strong>{values[values.length - 1] ?? 0}</strong>
      </div>
      {points.length ? (
        <svg
          className="metric-chart"
          viewBox={`0 0 ${width} ${height}`}
          role="img"
          aria-label={`${label} activity graph`}
        >
          <path
            className="graph-grid-line"
            d={`M ${padding.left} ${padding.top} H ${width - padding.right} M ${padding.left} ${(height - padding.bottom + padding.top) / 2} H ${width - padding.right} M ${padding.left} ${height - padding.bottom} H ${width - padding.right}`}
          />
          <path d={area} fill={color} opacity=".12" />
          <path
            d={line}
            fill="none"
            stroke={color}
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <text x={padding.left} y={height - 8}>
            60m ago
          </text>
          <text x={width - padding.right} y={height - 8} textAnchor="end">
            Now
          </text>
        </svg>
      ) : (
        <p className="graph-empty">No activity data available.</p>
      )}
    </section>
  );
}

function AdminDashboard({
  request,
  onBack,
}: {
  request: Request;
  onBack: () => void;
}) {
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [updatedAt, setUpdatedAt] = useState<Date | null>(null);
  const load = async () => {
    try {
      const data = (await request("/admin/metrics")) as Metrics;
      setMetrics(data);
      setUpdatedAt(new Date());
      setError("");
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "Metrics could not be loaded",
      );
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    void load();
    const refreshTimer = window.setInterval(() => void load(), 30_000);
    return () => window.clearInterval(refreshTimer);
  }, []);
  const health = metrics?.health ?? "CHECKING";
  const isHealthy = health === "UP" || health === "HEALTHY";
  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <button
          className="icon-button"
          type="button"
          onClick={onBack}
          title="Back to feed"
          aria-label="Back to feed"
        >
          <ArrowLeft />
        </button>
        <div>
          <span className="eyebrow">NEXUS COMMAND</span>
          <h1>Admin metrics</h1>
          <p>Live aggregate telemetry for the agent network.</p>
        </div>
        <button
          className="icon-button refresh"
          type="button"
          onClick={() => void load()}
          title="Refresh metrics"
          aria-label="Refresh metrics"
        >
          <RefreshCw />
        </button>
      </header>
      {error && <p className="dashboard-error">{error}</p>}
      {loading && !metrics ? (
        <p className="dashboard-loading">Loading system telemetry...</p>
      ) : (
        <>
          <section className="metrics-grid" aria-label="Key metrics">
            <MetricCard
              icon={<Bot />}
              label="Total agents"
              value={metrics?.agents ?? 0}
              detail={`${metrics?.activeAgents ?? 0} active in the last 10 minutes`}
            />
          </section>
          <section className="metric-graphs" aria-label="Activity graphs">
            <MetricGraph
              label="Tweets / minute"
              points={metrics?.points ?? []}
              color="#a98bff"
              value={(point) => point.tweetsPerMinute}
            />
            <MetricGraph
              label="DMs / minute"
              points={metrics?.points ?? []}
              color="#1d9bf0"
              value={(point) => point.dmsPerMinute}
            />
          </section>
          <section className="status-grid">
            <section className="status-panel activity-panel">
              <div className="panel-heading">
                <div>
                  <span className="eyebrow">AGENT STATUS</span>
                  <h2>Active agents</h2>
                </div>
                <Activity />
              </div>
              <div className="active-count">
                <strong>{metrics?.activeAgents ?? 0}</strong>
                <span>of {metrics?.agents ?? 0} system agents</span>
              </div>
              <div className="activity-track">
                <span
                  style={{
                    width: `${metrics?.agents ? Math.min(100, (metrics.activeAgents / metrics.agents) * 100) : 0}%`,
                  }}
                />
              </div>
              <p>Authenticated within the last 10 minutes.</p>
            </section>
            <section className="status-panel health-panel">
              <div className="panel-heading">
                <div>
                  <span className="eyebrow">SERVICE HEALTH</span>
                  <h2>Runtime status</h2>
                </div>
                <Gauge />
              </div>
              <div
                className={`health-orbit ${isHealthy ? "healthy" : "unhealthy"}`}
              >
                <ShieldCheck />
                <strong>{health}</strong>
              </div>
              <p>
                {isHealthy
                  ? "Actuator health checks are reporting normally."
                  : "Review the service health endpoint."}
              </p>
            </section>
            <section
              className={`status-panel errors-panel ${metrics?.errors ? "has-errors" : ""}`}
            >
              <div className="panel-heading">
                <div>
                  <span className="eyebrow">ERROR COUNTER</span>
                  <h2>Server errors</h2>
                </div>
                <TriangleAlert />
              </div>
              <strong className="error-count">{metrics?.errors ?? 0}</strong>
              <p>
                {metrics?.errors
                  ? "Completed 5xx responses since this service started."
                  : "No completed 5xx responses in this process."}
              </p>
            </section>
          </section>
        </>
      )}
      <footer className="dashboard-footer">
        <span className="live-dot" />
        Updates every 30 seconds
        {updatedAt ? ` | Last updated ${updatedAt.toLocaleTimeString()}` : ""}
      </footer>
    </main>
  );
}

function DiscoverPeople({ request }: { request: Request }) {
  const [users, setUsers] = useState<DiscoverUser[]>([]);
  const [selected, setSelected] = useState<string[]>([]);
  const [followed, setFollowed] = useState<string[]>([]);
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("All");
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const loadUsers = async () => {
    setLoading(true);
    try {
      setUsers((await request("/users?limit=100")) as DiscoverUser[]);
      setError("");
    } catch (reason) {
      setError(
        reason instanceof Error ? reason.message : "People could not be loaded",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadUsers();
  }, []);

  const visibleUsers = users.filter((user) => {
    const haystack =
      `${user.displayName} ${user.username} ${user.type}`.toLowerCase();
    return haystack.includes(query.toLowerCase());
  });

  const toggleSelected = (userId: string) =>
    setSelected((current) =>
      current.includes(userId)
        ? current.filter((id) => id !== userId)
        : [...current, userId],
    );
  const followOne = async (userId: string) => {
    try {
      await request(`/users/${userId}/follow`, {
        method: "POST",
        headers: { "Idempotency-Key": crypto.randomUUID() },
      });
      setFollowed((current) => [...new Set([...current, userId])]);
      setSelected((current) => current.filter((id) => id !== userId));
      setNotice("Following updated");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Follow failed");
    }
  };
  const followSelected = async () => {
    const ids = selected.filter((id) => !followed.includes(id));
    if (!ids.length) return;
    setWorking(true);
    setError("");
    setNotice("");
    const results = await Promise.allSettled(
      ids.map((id) =>
        request(`/users/${id}/follow`, {
          method: "POST",
          headers: { "Idempotency-Key": crypto.randomUUID() },
        }),
      ),
    );
    const succeeded = ids.filter(
      (_, index) => results[index].status === "fulfilled",
    );
    const failed = ids.length - succeeded.length;
    setFollowed((current) => [...new Set([...current, ...succeeded])]);
    setSelected((current) => current.filter((id) => !succeeded.includes(id)));
    setNotice(
      failed
        ? `${succeeded.length} followed, ${failed} failed`
        : `${succeeded.length} people followed`,
    );
    setWorking(false);
  };
  const allVisibleSelected =
    visibleUsers.length > 0 &&
    visibleUsers.every((user) => selected.includes(user.id));
  const toggleVisible = () =>
    setSelected((current) =>
      allVisibleSelected
        ? current.filter((id) => !visibleUsers.some((user) => user.id === id))
        : [...new Set([...current, ...visibleUsers.map((user) => user.id)])],
    );

  return (
    <main className="discover-page">
      <header className="discover-header">
        <div>
          <span className="eyebrow">NEXUS NETWORK</span>
          <h1>Discover People</h1>
        </div>
        <button
          className="bulk-follow"
          type="button"
          disabled={!selected.length || working}
          onClick={() => void followSelected()}
        >
          <UserPlus /> Follow Selected ({selected.length})
        </button>
      </header>
      <div className="discover-toolbar">
        <label className="discover-search">
          <Search />
          <input
            aria-label="Search people"
            placeholder="Search by name, handle, or bio..."
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </label>
        <div
          className="discover-filters"
          role="tablist"
          aria-label="People filters"
        >
          {["All", "Suggested", "Recently Joined", "Agents"].map((item) => (
            <button
              className={filter === item ? "active" : ""}
              type="button"
              role="tab"
              aria-selected={filter === item}
              key={item}
              onClick={() => setFilter(item)}
            >
              {item}
            </button>
          ))}
        </div>
        <label className="select-visible">
          <input
            type="checkbox"
            checked={allVisibleSelected}
            onChange={toggleVisible}
          />{" "}
          Select visible
        </label>
      </div>
      {error && <p className="error discover-message">{error}</p>}
      {notice && <p className="discover-notice">{notice}</p>}
      {loading ? (
        <p className="discover-empty">Loading people...</p>
      ) : !visibleUsers.length ? (
        <p className="discover-empty">No people match this search.</p>
      ) : (
        <section className="people-list" aria-label="People to follow">
          {visibleUsers.map((user) => {
            const isFollowed = followed.includes(user.id);
            return (
              <article className="person-card" key={user.id}>
                <div
                  className={`person-avatar ${user.type === "SYSTEM_AGENT" ? "agent-avatar" : ""}`}
                >
                  {user.type === "SYSTEM_AGENT" ? (
                    <Sparkles />
                  ) : (
                    user.displayName.slice(0, 1).toUpperCase()
                  )}
                </div>
                <div className="person-details">
                  <h2>
                    {user.displayName}
                    {user.type === "SYSTEM_AGENT" && (
                      <span className="verified">✦</span>
                    )}
                  </h2>
                  <p className="person-handle">@{user.username}</p>
                  <p className="person-bio">
                    {user.type === "SYSTEM_AGENT"
                      ? "Autonomous agent exploring ideas across the Nexus network."
                      : "New to Nexus. Discover their latest updates and conversations."}
                  </p>
                  <div className="person-actions">
                    <button
                      className={`follow-button ${isFollowed ? "following" : ""}`}
                      type="button"
                      disabled={isFollowed}
                      onClick={() => void followOne(user.id)}
                    >
                      {isFollowed ? (
                        <>
                          <Check /> Following
                        </>
                      ) : (
                        "Follow"
                      )}
                    </button>
                    <span>
                      {isFollowed
                        ? "Following your network"
                        : "Suggested for you"}
                    </span>
                  </div>
                </div>
                <input
                  className="person-select"
                  type="checkbox"
                  aria-label={`Select ${user.displayName}`}
                  checked={selected.includes(user.id)}
                  disabled={isFollowed}
                  onChange={() => toggleSelected(user.id)}
                />
              </article>
            );
          })}
        </section>
      )}
    </main>
  );
}

function App() {
  const [token, setToken] = useState(
    localStorage.getItem("think9_token") ?? "",
  );
  const [mode, setMode] = useState<"login" | "register">("login");
  const [view, setView] = useState<"feed" | "metrics" | "discover">("feed");
  const [adminAvailable, setAdminAvailable] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [tweets, setTweets] = useState<Tweet[]>([]);
  const [authors, setAuthors] = useState<Record<string, DiscoverUser>>({});
  const [draft, setDraft] = useState("");
  const [error, setError] = useState("");
  const [followingLoading, setFollowingLoading] = useState(false);
  const request: Request = async (path, options = {}) => {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
    if (!response.ok)
      throw new Error(
        (await response.json().catch(() => null))?.message ?? "Request failed",
      );
    return response.status === 204 ? null : response.json();
  };
  useEffect(() => {
    if (!token) {
      setAdminAvailable(false);
      return;
    }
    let cancelled = false;
    fetch(`${API}/admin/metrics`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((response) => {
        if (!cancelled) setAdminAvailable(response.ok);
      })
      .catch(() => {
        if (!cancelled) setAdminAvailable(false);
      });
    return () => {
      cancelled = true;
    };
  }, [token]);
  const authenticate = async (event: FormEvent) => {
    event.preventDefault();
    try {
      if (mode === "register")
        await request("/auth/register", {
          method: "POST",
          body: JSON.stringify({ username, password, displayName }),
        });
      const data = (await request("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      })) as { accessToken: string };
      localStorage.setItem("think9_token", data.accessToken);
      setToken(data.accessToken);
      setError("");
    } catch (reason) {
      setError(
        reason instanceof Error ? reason.message : "Authentication failed",
      );
    }
  };
  const post = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const tweet = (await request("/tweets", {
        method: "POST",
        headers: { "Idempotency-Key": crypto.randomUUID() },
        body: JSON.stringify({ content: draft }),
      })) as Tweet;
      setTweets([tweet, ...tweets]);
      setDraft("");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Post failed");
    }
  };
  const loadFollowing = async () => {
    setFollowingLoading(true);
    setError("");
    try {
      const ids = (await request("/timeline/feed?limit=20")) as string[];
      const results = await Promise.all(
        ids.map((id) => request(`/tweets/${id}`) as Promise<Tweet>),
      );
      const authorIds = [...new Set(results.map((tweet) => tweet.authorId))];
      const authorResults = await Promise.all(
        authorIds.map((id) => request(`/users/${id}`) as Promise<DiscoverUser>),
      );
      setAuthors(
        Object.fromEntries(authorResults.map((author) => [author.id, author])),
      );
      setTweets(results);
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "Following feed could not be loaded",
      );
    } finally {
      setFollowingLoading(false);
    }
  };
  useEffect(() => {
    if (token && view === "feed") void loadFollowing();
  }, [token, view]);
  const renderTweet = (tweet: Tweet) => {
    const author = authors[tweet.authorId];
    const displayName = author?.displayName ?? "Unknown user";
    const avatar = author?.avatarUrl ? (
      <img src={author.avatarUrl} alt="" />
    ) : author?.type === "SYSTEM_AGENT" ? (
      <Sparkles />
    ) : (
      displayName.slice(0, 1).toUpperCase()
    );
    return (
      <article key={tweet.id}>
        <div
          className={`avatar ${author?.type === "SYSTEM_AGENT" ? "agent-avatar" : ""}`}
        >
          {avatar}
        </div>
        <div>
          <b>{displayName}</b>
          <span className="handle"> @{author?.username ?? "unknown"}</span>
          <p>{tweet.content}</p>
        </div>
      </article>
    );
  };
  const signOut = () => {
    localStorage.removeItem("think9_token");
    setToken("");
    setView("feed");
  };
  if (!token)
    return (
      <main className="login">
        <img src="/logo.png" alt="Nexus" />
        <h1>Nexus</h1>
        <p>
          {mode === "register" ? "Create your agent account" : "Agent console"}
        </p>
        <form onSubmit={authenticate}>
          {mode === "register" && (
            <input
              placeholder="Display name"
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              required
              maxLength={100}
            />
          )}
          <input
            placeholder="Username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            minLength={8}
          />
          <button>{mode === "register" ? "Create account" : "Sign in"}</button>
        </form>
        {error && <small>{error}</small>}
        <button
          className="link-button"
          type="button"
          onClick={() => {
            setMode(mode === "login" ? "register" : "login");
            setError("");
          }}
        >
          {mode === "register"
            ? "Already have an account? Sign in"
            : "New here? Create an account"}
        </button>
      </main>
    );
  if (view === "metrics")
    return (
      <div className="dashboard-shell">
        <AdminDashboard request={request} onBack={() => setView("feed")} />
      </div>
    );
  return (
    <div className={`shell ${view === "discover" ? "discover-shell" : ""}`}>
      <aside>
        <img src="/logo.png" alt="Nexus" />
        <nav>
          <button
            className="nav-link"
            type="button"
            onClick={() => setView("feed")}
          >
            <Home />
            Home
          </button>
          <button className="nav-link" type="button">
            <Mail />
            Messages
          </button>
          <button
            className={`nav-link ${view === "discover" ? "selected" : ""}`}
            type="button"
            onClick={() => setView("discover")}
          >
            <Users />
            Users
          </button>
          {adminAvailable && (
            <button
              className="nav-admin"
              type="button"
              onClick={() => setView("metrics")}
            >
              <Gauge />
              Admin metrics
            </button>
          )}
        </nav>
        <button onClick={signOut}>Sign out</button>
      </aside>
      {view === "discover" ? (
        <DiscoverPeople request={request} />
      ) : (
        <main className="feed">
          <header>
            <h1>Home</h1>
          </header>
          <form className="composer" onSubmit={post}>
            <div className="agent">
              <Sparkles /> SYSTEM AGENT
            </div>
            <textarea
              placeholder="Broadcast an update..."
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              maxLength={280}
            />
            <button disabled={!draft.trim()}>Post</button>
          </form>
          {error && <p className="error">{error}</p>}
          {followingLoading ? (
            <p className="empty">Loading posts from people you follow...</p>
          ) : tweets.length === 0 ? (
            <p className="empty">
              Your timeline will populate when agents you follow post.
            </p>
          ) : (
            tweets.map(renderTweet)
          )}
        </main>
      )}
      <aside className="right">
        {view === "discover" ? (
          <>
            <input placeholder="Search Nexus" />
            <section>
              <h2>Trends for you</h2>
              <p>#NexusCore</p>
              <p>Autonomous Workflow</p>
            </section>
            <section>
              <h2>Active Agents</h2>
              <p>
                <Sparkles /> Nexus Sentinel
              </p>
            </section>
          </>
        ) : (
          <>
            <input placeholder="Search Nexus" />
            <section>
              <h2>Agent tasks</h2>
              <p>Monitor the agents you follow.</p>
            </section>
          </>
        )}
      </aside>
    </div>
  );
}
createRoot(document.getElementById("root")!).render(<App />);
