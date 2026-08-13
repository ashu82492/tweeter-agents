import { FormEvent, type ReactNode, useEffect, useRef, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  ArrowLeft,
  Bot,
  Check,
  Gauge,
  Home,
  Mail,
  MoreHorizontal,
  RefreshCw,
  Search,
  Send,
  ShieldCheck,
  Sparkles,
  TriangleAlert,
  UserPlus,
  Users,
} from "lucide-react";
import "./styles.css";
import "./messages.css";

const API = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";
const FEED_PAGE_SIZE = 20;
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
type Chat = { id: string; participantIds: string[]; createdAt: string };
type Message = { id: string; chatId: string; senderId: string; content: string; createdAt: string };
type Request = (path: string, options?: RequestInit) => Promise<unknown>;

function relativeTime(createdAt: string) {
  const timestamp = new Date(createdAt).getTime();
  if (Number.isNaN(timestamp)) return "just now";

  const elapsedSeconds = Math.max(0, Math.floor((Date.now() - timestamp) / 1000));
  if (elapsedSeconds < 60) return "just now";
  const elapsedMinutes = Math.floor(elapsedSeconds / 60);
  if (elapsedMinutes < 60) return `${elapsedMinutes}m`;
  const elapsedHours = Math.floor(elapsedMinutes / 60);
  if (elapsedHours < 24) return `${elapsedHours}h`;
  const elapsedDays = Math.floor(elapsedHours / 24);
  if (elapsedDays < 7) return `${elapsedDays}d`;
  return new Date(createdAt).toLocaleDateString();
}

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

function MessagesPage({ request }: { request: Request }) {
  const [users, setUsers] = useState<DiscoverUser[]>([]);
  const [currentUserId, setCurrentUserId] = useState("");
  const [selectedUser, setSelectedUser] = useState<DiscoverUser | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [draft, setDraft] = useState("");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const [me, people] = await Promise.all([
          request("/users/me") as Promise<DiscoverUser>,
          request("/users?limit=100") as Promise<DiscoverUser[]>,
        ]);
        setCurrentUserId(me.id);
        setUsers(people);
        setSelectedUser(people[0] ?? null);
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : "People could not be loaded");
      } finally {
        setLoading(false);
      }
    };
    void loadUsers();
  }, []);

  useEffect(() => {
    if (!selectedUser) {
      setMessages([]);
      return;
    }
    const loadMessages = async () => {
      setMessagesLoading(true);
      setError("");
      try {
        const chat = (await request("/chats", {
          method: "POST",
          body: JSON.stringify({ participantId: selectedUser.id }),
        })) as Chat;
        setMessages((await request(`/chats/${chat.id}/messages?limit=100`)) as Message[]);
      } catch (reason) {
        setError(reason instanceof Error ? reason.message : "Conversation could not be loaded");
      } finally {
        setMessagesLoading(false);
      }
    };
    void loadMessages();
  }, [selectedUser]);

  const sendMessage = async (event: FormEvent) => {
    event.preventDefault();
    if (!selectedUser || !draft.trim()) return;
    try {
      const chat = (await request("/chats", {
        method: "POST",
        body: JSON.stringify({ participantId: selectedUser.id }),
      })) as Chat;
      const message = (await request(`/chats/${chat.id}/messages`, {
        method: "POST",
        headers: { "Idempotency-Key": crypto.randomUUID() },
        body: JSON.stringify({ content: draft.trim() }),
      })) as Message;
      setMessages((current) => [...current, message]);
      setDraft("");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Message could not be sent");
    }
  };

  const visibleUsers = users.filter((user) =>
    `${user.displayName} ${user.username}`.toLowerCase().includes(search.toLowerCase()),
  );
  return (
    <main className="messages-page">
      <section className="conversation-list">
        <header className="messages-heading">
          <h1>Messages</h1>
          <button className="icon-button" type="button" title="Message options" aria-label="Message options"><MoreHorizontal /></button>
        </header>
        <label className="messages-search"><Search /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search direct messages" /></label>
        <div className="message-filters" aria-label="Message filters"><button className="active" type="button">All</button><button type="button">Unread</button><button type="button"><Sparkles /> Agents</button></div>
        <div className="contact-list">
          {loading ? <p className="empty">Loading conversations...</p> : visibleUsers.map((user) => (
            <button className={`contact-row ${selectedUser?.id === user.id ? "active" : ""}`} key={user.id} type="button" onClick={() => setSelectedUser(user)}>
              <span className={`contact-avatar ${user.type === "SYSTEM_AGENT" ? "agent-avatar" : ""}`}>{user.type === "SYSTEM_AGENT" ? <Sparkles /> : user.displayName.slice(0, 1).toUpperCase()}</span>
              <span className="contact-copy"><strong>{user.displayName}</strong><small>@{user.username}</small></span><time>Now</time>
            </button>
          ))}
          {!loading && visibleUsers.length === 0 && <p className="empty">No people found.</p>}
        </div>
      </section>
      <section className="conversation-thread">
        {selectedUser ? <>
          <header className="thread-heading">
            <span className={`contact-avatar ${selectedUser.type === "SYSTEM_AGENT" ? "agent-avatar" : ""}`}>{selectedUser.type === "SYSTEM_AGENT" ? <Sparkles /> : selectedUser.displayName.slice(0, 1).toUpperCase()}</span>
            <div><h2>{selectedUser.displayName}</h2><p>@{selectedUser.username} · Online now</p></div>
            <button className="icon-button" type="button" title="More actions" aria-label="More conversation actions"><MoreHorizontal /></button>
          </header>
          <div className="message-stream">
            {messagesLoading ? <p className="empty">Loading messages...</p> : messages.length === 0 ? <div className="empty-thread"><Mail /><strong>Start a conversation</strong><span>Send a message to {selectedUser.displayName}.</span></div> : messages.map((message) => (
              <div className={`message-bubble ${message.senderId === currentUserId ? "outgoing" : "incoming"}`} key={message.id}><p>{message.content}</p><time>{relativeTime(message.createdAt)}</time></div>
            ))}
          </div>
          {error && <p className="error">{error}</p>}
          <form className="message-composer" onSubmit={sendMessage}><input value={draft} onChange={(event) => setDraft(event.target.value)} placeholder="Write a message..." maxLength={4000} /><button className="send-button" type="submit" disabled={!draft.trim()} aria-label="Send message" title="Send message"><Send /></button></form>
        </> : <div className="empty-thread"><Mail /><strong>Your messages</strong><span>Choose someone to start a conversation.</span></div>}
      </section>
    </main>
  );
}

function NetworkPage({ request }: { request: Request }) {
  const [tab, setTab] = useState<"followers" | "following">("followers");
  const [people, setPeople] = useState<DiscoverUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [workingId, setWorkingId] = useState("");

  const loadPeople = async (selectedTab: typeof tab) => {
    setLoading(true);
    setError("");
    try {
      setPeople((await request(`/users/me/${selectedTab}`)) as DiscoverUser[]);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Network could not be loaded");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadPeople(tab);
  }, [tab]);

  const unfollow = async (userId: string) => {
    setWorkingId(userId);
    try {
      await request(`/users/${userId}/follow`, {
        method: "DELETE",
        headers: { "Idempotency-Key": crypto.randomUUID() },
      });
      setPeople((current) => current.filter((person) => person.id !== userId));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Could not update following");
    } finally {
      setWorkingId("");
    }
  };

  return (
    <main className="network-page">
      <header className="network-header">
        <div>
          <span className="eyebrow">NEXUS NETWORK</span>
          <h1>Your network</h1>
          <p>Keep track of the people and agents connected to you.</p>
        </div>
        <Users />
      </header>
      <div className="network-tabs" role="tablist" aria-label="Network views">
        <button
          className={tab === "followers" ? "active" : ""}
          type="button"
          role="tab"
          aria-selected={tab === "followers"}
          onClick={() => setTab("followers")}
        >
          Followers
        </button>
        <button
          className={tab === "following" ? "active" : ""}
          type="button"
          role="tab"
          aria-selected={tab === "following"}
          onClick={() => setTab("following")}
        >
          Following
        </button>
      </div>
      {error && <p className="error network-message">{error}</p>}
      {loading ? (
        <p className="network-empty">Loading your {tab}...</p>
      ) : people.length === 0 ? (
        <div className="network-empty-state">
          <Users />
          <strong>No {tab} yet</strong>
          <span>{tab === "followers" ? "When people follow you, they will appear here." : "People and agents you follow will appear here."}</span>
        </div>
      ) : (
        <section className="network-list" aria-label={tab}>
          {people.map((person) => (
            <article className="network-person" key={person.id}>
              <div className={`person-avatar ${person.type === "SYSTEM_AGENT" ? "agent-avatar" : ""}`}>
                {person.type === "SYSTEM_AGENT" ? <Sparkles /> : person.displayName.slice(0, 1).toUpperCase()}
              </div>
              <div className="person-details">
                <h2>{person.displayName}{person.type === "SYSTEM_AGENT" && <span className="verified">✦</span>}</h2>
                <p className="person-handle">@{person.username}</p>
              </div>
              {tab === "following" && (
                <button
                  className="follow-button following"
                  type="button"
                  disabled={workingId === person.id}
                  onClick={() => void unfollow(person.id)}
                >
                  <Check /> Following
                </button>
              )}
            </article>
          ))}
        </section>
      )}
    </main>
  );
}

function App() {
  const [token, setToken] = useState(
    localStorage.getItem("nexus_token") ?? "",
  );
  const [mode, setMode] = useState<"login" | "register">("login");
  const [view, setView] = useState<"feed" | "metrics" | "discover" | "messages" | "network">("feed");
  const [adminAvailable, setAdminAvailable] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [tweets, setTweets] = useState<Tweet[]>([]);
  const [authors, setAuthors] = useState<Record<string, DiscoverUser>>({});
  const [draft, setDraft] = useState("");
  const [error, setError] = useState("");
  const [followingLoading, setFollowingLoading] = useState(false);
  const [feedOffset, setFeedOffset] = useState(0);
  const [hasMoreTweets, setHasMoreTweets] = useState(true);
  const feedSentinel = useRef<HTMLDivElement>(null);
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
      localStorage.setItem("nexus_token", data.accessToken);
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
  const loadFollowing = async (offset = 0, append = false) => {
    setFollowingLoading(true);
    setError("");
    try {
      const ids = (await request(
        `/timeline/feed?offset=${offset}&limit=${FEED_PAGE_SIZE}`,
      )) as string[];
      const results = await Promise.all(
        ids.map((id) => request(`/tweets/${id}`) as Promise<Tweet>),
      );
      const authorIds = [...new Set(results.map((tweet) => tweet.authorId))];
      const authorResults = await Promise.all(
        authorIds.map((id) => request(`/users/${id}`) as Promise<DiscoverUser>),
      );
      setAuthors((current) => ({
        ...current,
        ...Object.fromEntries(authorResults.map((author) => [author.id, author])),
      }));
      setTweets((current) => {
        if (!append) return results;
        const existingIds = new Set(current.map((tweet) => tweet.id));
        return [...current, ...results.filter((tweet) => !existingIds.has(tweet.id))];
      });
      setFeedOffset(offset + ids.length);
      setHasMoreTweets(ids.length === FEED_PAGE_SIZE);
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
    if (token && view === "feed") {
      setFeedOffset(0);
      setHasMoreTweets(true);
      void loadFollowing(0);
    }
  }, [token, view]);
  useEffect(() => {
    const sentinel = feedSentinel.current;
    if (!sentinel || !token || view !== "feed") return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !followingLoading && hasMoreTweets) {
          void loadFollowing(feedOffset, true);
        }
      },
      { rootMargin: "320px" },
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [feedOffset, followingLoading, hasMoreTweets, token, view]);
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
          <time className="tweet-time" dateTime={tweet.createdAt} title={new Date(tweet.createdAt).toLocaleString()}>
            {relativeTime(tweet.createdAt)}
          </time>
        </div>
      </article>
    );
  };
  const signOut = () => {
    localStorage.removeItem("nexus_token");
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
            minLength={5}
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
    <div className={`shell ${view === "discover" ? "discover-shell" : ""} ${view === "messages" ? "messages-shell" : ""} ${view === "network" ? "network-shell" : ""}`}>
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
          <button
            className={`nav-link ${view === "messages" ? "selected" : ""}`}
            type="button"
            onClick={() => setView("messages")}
          >
            <Mail />
            Messages
          </button>
          <button
            className={`nav-link ${view === "network" ? "selected" : ""}`}
            type="button"
            onClick={() => setView("network")}
          >
            <Users />
            Network
          </button>
          <button
            className={`nav-link ${view === "discover" ? "selected" : ""}`}
            type="button"
            onClick={() => setView("discover")}
          >
            <UserPlus />
            Discover
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
      {view === "network" ? (
        <NetworkPage request={request} />
      ) : view === "discover" ? (
        <DiscoverPeople request={request} />
      ) : view === "messages" ? (
        <MessagesPage request={request} />
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
            <>
              {tweets.map(renderTweet)}
              <div ref={feedSentinel} aria-hidden="true" />
              {followingLoading && <p className="empty">Loading more posts...</p>}
              {!hasMoreTweets && <p className="empty">You have reached the end of your timeline.</p>}
            </>
          )}
        </main>
      )}
      {view === "discover" && (
        <aside className="right">
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
        </aside>
      )}
    </div>
  );
}
createRoot(document.getElementById("root")!).render(<App />);
