Create a Backend service in java with spring. 
1. Use /spring-boot skill to follow the best practices for java and spring boot. 
2. Add minimal tests also, use skill /java-junit to follow the best practices.
3. Consider the below business entities, with main fields mentioned below, add remaining by yourself like created at etc.
    Users
        - user type - Human, System Agent, don't build anything for Human type user for now.
    Tweet
        - author id
        - content
    Follow 
        - follower_id
        - followee_id
    Chat
        - a set of participants (keep only 2 for now)
    Message
        - sender_id
        - chat_id
        - content
4. Create a authentication mechanism based on the username and password. Create JWT token with appropriate claims on succesful login.
5. make sure to have proper authorization using user id on profile/private/DMs data fetched via APIs. If there is a confusion wether to add auth or not an API, ask explicitly. Don't decide on your own.
6. Create the below services with interfaces and it's implementations. I have mentioned few necessary example interface methods, add more as needed. All interface implementations for write actions must be idempotent, retryable.
    UserService
        create()
        get()
    AuthenticationService
    TweetService
        tweet(content)
        getTweet(tweet_id)
        getTweet(user_id)
    FollowService
        follow()
        unfollow()
    MessagingService
        createChat()
        message()
        read()
    TimelineService
        fetchFeed()
        create()
7. User and follow are part of one java module. Messaging is a separate java modules. Tweet related module is a separate.
8. Create a core module for common infra services if any, e.g. EventPublisher.
9. EventPublisher publishes events for each tweet and follower activity (follow/unfollow). There can be subscriber to those events. Use Spring for pub sub. Keep the 
9. Create a module which is separately deployable than the main service.
    - this is for building the timeline for each user.
    - Add a subscriber to tweet and follow/unfollow events.
        - On TWEET events, add the tweet to all the followers of the author.
        - On FOLLOW events, add recent (configurable) tweets of the followeer.
    - The service/business logic must not be tightly coupled with Spring pub/sub, build it behind proper intfaces/abstractions. It should be replacable with Kafka/RabbitMq/Distributed platform later.
10. Create the below repositories, behind interfaces, in their respective modules.
    TweetRepository -> Redis Cache for each tweet -> Mysql (Could be a sharded on author_id with sql or choose nosql, keep it simple mysql for now)
    FollowRepository -> Mysql
    ChatRepository - 
    MessageRepository -> Mysql (could be shared on the chat_id or use nosql, but keep it simple to Mysql for now)
    TimelineRepository -> redis (only tweet_ids). The client would call separate Api to fetch tweet content.
    UserRepository -> Mysql
11. Create a Frontend service with ./designs/ui-design.md UI/ux design, it has logo and feed page designs too. Discuss the tech stack before implementation. keep the folder for frontend separate.
12. Create a docker-compose file to deploy everything on local.







Build a dashboard for the metrics functionality for the admin user. A ui design is added in designs/analytics.png and designs/analytics_mobile.png.

Requirements:

Admin metrics panel — agents count, tweets/min, DMs/min, active agents, errors, health.
Create a metricService which returns the below.
1. total agent count, read from the users repository for user type = system agent.
2. total active agents.
    add a last_active time column in users table. Do a write to it on jwt authentication, cache it for next 5 minutes to avoid updating it back. Return the number of users where the now - last_active < 10 minutes

3. Track tweets/min, dm/min.
    Create a rolling window and track a in-memory counter (in metric service) whenever a new tweet/DM is created. Subscribe to Tweet events.
    return tweets/minutes for last 1 hour.

4. Track errors.
    create a spring handler for 5xx which increases the counter to track error.

5. Track health.
    use the spring health endpoint to show the current status of service health.

API
GET /admin/metrics

it should return the below example response:

{
"agents": 100,
"activeAgents": 97,
"tweetsPerMinute": [],
"dmsPerMinute": [],
"errors": 2,
"health": "HEALTHY"
}

handle the concurrent update for counters, etc. It must be thread safe.




twitter like backend and frontend is already present in this repository.

create a separate directory for agent management system. agents would be interacting with backend exactly like a real human users. 


0. Create a utility in java to 
    - create N number of agent users (N=100 for now) by a user name and password using register user API.
    - assign a personality  to each agent. Choose personality from static set of configuired personalities. Add few peronsalities in this config.
    - choose random agents to follow for each agent. Choose a randome number between 0-5 and follow those number of other agents.
1. Write an java orchestrator which does the following
    - There is an emum of actions (Tweet, DM), create this enum. 
    - Publish en event to kafka for 2 Tweet, 1 DM to a another random agent. These are only events (action, agent_id, metadata). Actual tweet and DM doesn't happen.
    - The kafka event must be partitioned by agent_id.
1a. create a schedular which calls the above orchestrator every 30 seconds (configurable).
2. add a kafka image in docker compose for local development. Keep 50 partitions.
3. Create a consumer in java which 
    - reads from the kafka topic partition
    - as per action
        - if action = tweet
            then read it's recent timeline
            provide LLM this context of recent timelines to generate an tweet content.
            if the recent timeline is empty, then choose a random topic to tweet (can come from personallity and interest of this agent)
            tweet using the backend api
        - if action = DM
            read the recent DMs with the other agent
            add these recent DMs to LLM to genereate the next DM
            use DM backend api to send the DM.
4. Create a LLM client to call local LM studio API, it's openAI chat compatible.

