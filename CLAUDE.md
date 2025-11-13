# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an AI Companion Mod for Minecraft - a two-part system that creates natural, intelligent AI companions that can interact with players in the game.

**Architecture**: 
- **Fabric Mod** (Java): Client-side mod that runs in Minecraft
- **AI Service** (Python/FastAPI): Local service that handles LLM interactions and decision-making
- **Communication**: WebSocket connection between Mod and Service

**Current Status**: Design phase - comprehensive documentation exists but code implementation is pending.

## ⚠️ CRITICAL RULES

**Git Repository Management**:

1. **NEVER initialize or operate git in the Project folder (`G:/Minecraft/Project/`)**
   - The Project folder is for local storage only
   - Do NOT run `git init`, `git add`, `git commit`, or any git commands in this directory
   - Do NOT create `.git` folder or `.gitignore` in the Project root

2. **Git repository ONLY exists in `ai-companion-mod/` subfolder**
   - All git operations must be performed inside `ai-companion-mod/` directory
   - Commands: `cd ai-companion-mod && git <command>`
   - Remote repository: https://github.com/ICE6332/MineCompanion-BOT

3. **Always ask user before making changes to Project folder**
   - Any file creation, deletion, or modification in Project root
   - Any structural changes to the Project directory
   - Moving or copying files between directories

**Violation of these rules can result in data loss.**

## Project Structure

```
Project/
├── docs/                              # Complete design documentation
│   ├── 01-架构设计.md                 # System architecture and design
│   ├── 02-Fabric模组实现.md           # Fabric mod implementation details
│   ├── 03-本地AI服务.md               # AI service design
│   ├── 04-API接口文档.md              # API/protocol documentation
│   └── 05-开发路线图.md               # Development roadmap
├── MineCompanion-BOT/                  # Fabric Mod source (to be created)
└── ai-service/                        # Python AI service (to be created)
```


## Architecture

### Two-Component System

**Mod Component (Java/Fabric)**:
- `AICompanionEntity` - Main entity class that exists in game world
- `SmartFollowGoal` - Intelligent following behavior with distance-based strategies
- `ConversationTrigger` - Determines when AI should initiate conversation
- `WebSocketClient` - Communication with AI service
- `GameStateCollector` - Gathers game state for AI context

**AI Service Component (Python)**:
- `LLMInterface` - Unified interface supporting multiple LLM providers (OpenAI, Claude, Ollama, etc.)
- `PersonalityEngine` - Manages AI personality configurations
- `MemoryManager` - Short/medium/long-term memory system
- `ContextBuilder` - Builds rich context for LLM prompts

### Communication Protocol

WebSocket messages follow this format:
```json
{
  "id": "message_unique_id",
  "type": "conversation_request|action_decision_request|game_state_update",
  "timestamp": "ISO8601",
  "data": {...}
}
```

Key message types:
- `conversation_request` - Request AI to generate dialogue
- `action_decision_request` - Request AI to decide next behavior
- `game_state_update` - Sync game state to AI service

## Design Principles

1. **Natural Interaction**: AI should feel like a real companion, not a robot
   - Proactive conversation (time-based, event-triggered, context-aware)
   - Random idle behaviors (looking around, jumping, picking flowers)
   - Relationship progression (stranger → friend → best friend)

2. **Smart Following**: Distance-based following strategies
   - Close (0-5 blocks): Wander nearby, give player space
   - Medium (5-15 blocks): Relaxed following, can do side activities
   - Far (15+ blocks): Fast pathfinding to catch up

3. **Collaborative Activities**: AI assists with player activities
   - Mining: Mine nearby, share discoveries, provide lighting
   - Building: Observe, suggest, collect materials
   - Combat: Auto-attack hostiles, tactical positioning
   - Exploring: Mark points of interest, share observations

4. **Local & Privacy-Focused**: All data processed locally, user controls LLM choice

## Development Workflow

### Phase 1: Foundation (Weeks 1-2)
- Set up Fabric mod project with Gradle
- Create basic `AICompanionEntity` with rendering and movement
- Build Python FastAPI service with WebSocket endpoint
- Implement LLM provider interfaces (OpenAI, Ollama)
- Test basic communication between Mod and Service

### Phase 2: Core Behaviors (Weeks 3-4)
- Implement `SmartFollowGoal` with distance-based logic
- Build pathfinding and obstacle avoidance
- Create `GameStateCollector` for game context
- Implement conversation generation with personality
- Add basic decision-making (rule-based + LLM)

### Phase 3: Natural Interaction (Weeks 5-6)
- Build `ConversationTrigger` with multiple trigger types
- Implement event listeners (player damage, achievements, blocks)
- Add `MemoryManager` for conversation history
- Create `RelationshipTracker` for relationship progression
- Enhance behavior with random actions and animations

### Phase 4: Collaborative Activities (Weeks 7-8)
- Implement activity recognition (mining, building, combat)
- Create specialized behavior classes (MiningBehavior, BuildingBehavior, etc.)
- Add inventory management and item exchange
- Build task planning system

### Phase 5: UI & Configuration (Weeks 9-10)
- Create config GUI using Cloth Config API
- Add in-game UI (chat bubbles, status HUD, relationship display)
- Implement multiple personality presets
- Support for multiple LLM providers with easy switching

### Phase 6: Polish (Weeks 11-12)
- Performance optimization (caching, async processing, reduced network calls)
- Comprehensive error handling and recovery
- Complete documentation (installation, configuration, troubleshooting)
- Testing across different scenarios and mod compatibility

## Key Implementation Details

### Mod Side (Java)

**Entity Goals Priority** (lower number = higher priority):
```java
0: SwimGoal
1: EscapeDangerGoal  
2: SurvivalGoal
3: CombatGoal
4: SmartFollowGoal
5: AssistPlayerGoal
6: IdleBehavior
```

**Distance-Based Following**:
- Use different speeds and pathfinding strategies based on distance
- Add randomness to avoid robotic behavior
- Include idle activities when close to player
- Teleport if distance > 50 blocks

**Conversation Triggering**:
- Base interval depends on relationship level (10min for stranger, 5min for friend)
- Calculate trigger probability based on context (player idle, interesting events, mood)
- Send rich context to AI service (time, weather, biome, recent events, conversation history)

### Service Side (Python)

**LLM Interface Design**:
- Abstract base class `BaseLLMProvider` 
- Provider-specific implementations (OpenAIProvider, OllamaProvider, etc.)
- Unified `generate()` and `chat()` methods
- Configuration loaded from YAML files

**Decision Making**:
- Simple decisions use rule-based engine (distance too far → follow)
- Complex decisions use LLM with structured prompts
- Cache similar responses to reduce API calls
- Async processing to avoid blocking

**Personality System**:
- YAML configurations define personality traits, conversation frequency, behavior preferences
- Prompts dynamically incorporate personality into LLM context
- Multiple preset personalities (cheerful companion, quiet miner, builder master, etc.)

## Important Notes

- **No code exists yet** - This is a design-phase project with comprehensive documentation
- When implementing, refer to detailed code examples in `docs/02-Fabric模组实现.md` and `docs/03-本地AI服务.md`
- API protocol is fully documented in `docs/04-API接口文档.md`
- Follow the phased approach in `docs/05-开发路线图.md` for implementation
- The architecture supports multiple LLM providers - always test with at least OpenAI and Ollama
- All user-facing text should support Chinese and English (i18n)

## Documentation Map

- **Architecture & Design**: Start with `docs/01-架构设计.md` for system overview
- **Mod Implementation**: See `docs/02-Fabric模组实现.md` for Java code structure and examples
- **AI Service**: See `docs/03-本地AI服务.md` for Python service architecture
- **API Protocol**: Reference `docs/04-API接口文档.md` for message formats and data models
- **Development Plan**: Follow `docs/05-开发路线图.md` for implementation phases

## Future Extensions

- Multi-AI support (multiple companions working together)
- Voice interaction (speech-to-text + TTS)
- More collaborative activities (farming, trading, exploration)
- MOD compatibility (Create, Botania, custom dimensions)
- Plugin system for custom behaviors
