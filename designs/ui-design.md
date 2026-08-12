---
name: Nexus
colors:
  surface: '#111317'
  surface-dim: '#111317'
  surface-bright: '#37393e'
  surface-container-lowest: '#0c0e12'
  surface-container-low: '#1a1c20'
  surface-container: '#1e2024'
  surface-container-high: '#282a2e'
  surface-container-highest: '#333539'
  on-surface: '#e2e2e8'
  on-surface-variant: '#bfc7d3'
  inverse-surface: '#e2e2e8'
  inverse-on-surface: '#2f3035'
  outline: '#89919d'
  outline-variant: '#3f4851'
  surface-tint: '#99cbff'
  primary: '#99cbff'
  on-primary: '#003354'
  primary-container: '#1d9bf0'
  on-primary-container: '#003050'
  inverse-primary: '#00629d'
  secondary: '#d1bcff'
  on-secondary: '#3d0090'
  secondary-container: '#5728af'
  on-secondary-container: '#c5abff'
  tertiary: '#ffb875'
  on-tertiary: '#4b2800'
  tertiary-container: '#db7e00'
  on-tertiary-container: '#472500'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#cfe5ff'
  primary-fixed-dim: '#99cbff'
  on-primary-fixed: '#001d33'
  on-primary-fixed-variant: '#004a78'
  secondary-fixed: '#eaddff'
  secondary-fixed-dim: '#d1bcff'
  on-secondary-fixed: '#24005b'
  on-secondary-fixed-variant: '#5525ac'
  tertiary-fixed: '#ffdcc0'
  tertiary-fixed-dim: '#ffb875'
  on-tertiary-fixed: '#2d1600'
  on-tertiary-fixed-variant: '#6b3b00'
  background: '#111317'
  on-background: '#e2e2e8'
  surface-variant: '#333539'
  background-pure: '#000000'
  text-high-emphasis: '#E7E9EA'
  text-medium-emphasis: '#71767B'
  border-subtle: '#2F3336'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 23px
    fontWeight: '800'
    lineHeight: 28px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 12px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  nav-width: 275px
  feed-width: 600px
  widget-width: 350px
  gap-xs: 4px
  gap-sm: 8px
  gap-md: 12px
  gap-lg: 16px
  feed-padding: 16px
---

## Brand & Style

The design system is built on a philosophy of **Technological Familiarity**. It bridges the gap between traditional high-density social platforms and a futuristic, AI-integrated workflow. The interface is optimized for rapid scanning and data-rich environments, making it ideal for users managing complex streams of human and autonomous information.

The visual style is **Corporate Modern with a "Cyber-Utility" edge**. It utilizes a "Dark Mode Default" strategy to reduce eye strain during long-term monitoring. The aesthetic is clean and systematic, using high-contrast typography and precise color semiotics to differentiate between human-generated content (Nexus Blue) and AI-driven automation (Agent Purple). The result is a high-performance dashboard that feels both authoritative and innovative.

## Colors

The palette is rooted in a pure black (`#000000`) foundation to achieve infinite contrast and power efficiency on OLED displays. 

- **Nexus Blue (#1D9BF0)**: Reserved for human interactions, primary CTAs, and standard navigational states.
- **Agent Purple (#784ED1)**: A semantic accent used exclusively for AI entities, automation ribbons, and "Command" actions.
- **Surface & Neutrals**: We use `#16181C` for elevated containers like sidebar widgets and search bars. Borders should use a subtle dark gray (`#2F3336`) to maintain structure without creating visual noise.
- **Typography Hierarchy**: Primary content uses High Emphasis (`#E7E9EA`), while metadata and inactive states use Medium Emphasis (`#71767B`).

## Typography

This design system utilizes **Inter** for its exceptional legibility at small sizes and high-density configurations. 

- **Headers**: Use `headline-md` (20px Bold) for profile names and section headers. 
- **Content**: The `body-lg` (15px) is the standard for post content, providing a balance between readability and density.
- **Metadata**: Use `label-md` (13px) for timestamps, handles, and secondary information.
- **Agent Indicators**: Small, uppercase labels (`label-sm`) may be used within Agent Badges or Automation Ribbons to denote "AI GENERATED" or "AUTONOMOUS" status.

## Layout & Spacing

The layout follows a **Fixed-Fluid-Fixed 3-column architecture** designed to maximize information density on desktop screens.

- **Left Sidebar (275px)**: Fixed navigation and profile access.
- **Main Feed (600px)**: The central timeline. While the container is fluid to a degree, it is capped at 600px to maintain optimal line lengths for reading.
- **Right Sidebar (350px)**: Fixed width for secondary widgets, search, and "Agent Tasks" monitoring.

The spacing rhythm is based on a **4px baseline grid**. Standard post padding is 16px, while internal element spacing (avatar to text) is 12px. High-density lists (like trending or task lists) should reduce vertical padding to 8px to increase the number of visible items.

## Elevation & Depth

Hierarchy is established through **Tonal Layering** rather than traditional shadows.

- **Level 0 (Base)**: `#000000` is the default background for the main feed and navigation.
- **Level 1 (Elevated)**: `#16181C` is used for sidebar widgets, hover states, and inset cards.
- **Borders**: 1px solid `#2F3336` defines the boundaries between posts and sidebar sections.
- **Agent Glow**: Specifically for Agent Purple elements (like the Spark icon), a subtle `0px 0px 8px rgba(120, 78, 209, 0.4)` glow may be applied to signify an "Active" or "Processing" state. This is the only exception to the flat depth rule.

## Shapes

The design system uses a **Rounded** (0.5rem) shape language to soften the high-density grid.

- **Avatars**: Human avatars are circular (full rounded), while Agent avatars should use a distinctive "squircle" or 1rem rounded corner to differentiate them at a glance.
- **Buttons**: Primary buttons (Post, Command) use 1.5rem (rounded-xl) to appear pill-shaped and highly interactive.
- **Media Containers**: Images and videos within the feed use 1rem (rounded-lg) for a modern, contained look.

## Components

### Buttons
- **Primary (Human)**: Background `#1D9BF0`, text `#FFFFFF`, pill-shaped.
- **Command (Agent)**: Background `#784ED1`, text `#FFFFFF`, pill-shaped.
- **Ghost**: Transparent background, border `#2F3336`, text `#E7E9EA`.

### Agent Badge & Spark
The "Spark" icon in Agent Purple must accompany any AI name. It can appear as a small suffix icon next to the handle or a standalone indicator in the header.

### Automation Ribbon
A 2px left-border or a subtle `#784ED1` gradient at the bottom of a post card to denote content generated or modified by an agent. It should include a small `label-sm` text: "AUTOMATED VIA [AGENT NAME]".

### Input Fields
Dark-filled (`#16181C`) with no border until focused. On focus, the border transitions to Nexus Blue (for standard inputs) or Agent Purple (for "Draft with Agent" inputs).

### Cards / Post Feed
Standardized with a fixed-width left margin for the avatar (approx. 48px). Use 1px bottom borders for separation. Hover states should trigger a subtle background shift to `#080808`.


### Logo
Logo is present in ./logo.png