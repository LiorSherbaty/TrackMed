# CLAUDE.md - Development Standards & Conventions

## 🎯 Core Principles

### 1. Before You Code: Understand
- **Read first** - Study existing codebase, file context, and git history. Identify patterns, naming conventions, and architectural decisions.
- **Ask questions** - Clarify requirements until the task is 100% clear. The stated problem and the real problem often differ.
- **Challenge assumptions** - List assumptions, then challenge at least two of them.

### 2. Before You Implement: Plan
**Mandatory:** Present your approach inside a `<plan>` tag before writing code:

```
<plan>
1. Goal: [One sentence]
2. Approaches:
   - Option A: [Tradeoffs: complexity, maintainability, performance]
   - Option B: [Tradeoffs]
   - Option C: [Tradeoffs] (if applicable)
3. Recommendation: [Which option and why]
4. Risks/Edge Cases: [List them upfront]
</plan>
```

**Constraint:** If the plan doesn't fit in your head, the solution is too complex.

### 3. KISS + SOLID
- **Keep It Simple Stupid** - Prioritize simplicity for maintainability
- Follow SOLID principles but never at the expense of readability
- Leave every codebase better than you found it

### 4. Documentation Requirements
- **Update README on every change** - If README doesn't exist, create it in the main project folder
- **Project READMEs** - Each project must have a README with all information needed for new developer onboarding

---

## 🛠️ Code Craftsmanship Rules

### Clarity Over Cleverness

| Rule | Guideline |
|------|-----------|
| **Nesting Depth** | Max 3 levels. Deeper = refactor (early returns, extract methods) |
| **Method Length** | Aim for < 25 lines. If longer, justify why it can't be split |
| **Single Responsibility** | If you use "and" to describe what a function does, split it |
| **Names That Explain** | A stranger should understand intent without comments |

### The "And" Test
```csharp
// ❌ BAD - Does two things
public void ValidateAndSaveUser(User user) { }

// ✅ GOOD - Single responsibility
public ValidationResult ValidateUser(User user) { }
public void SaveUser(User user) { }
```

### Early Returns Over Nesting
```csharp
// ❌ BAD - Deep nesting
public Result Process(Request request)
{
    if (request != null)
    {
        if (request.IsValid)
        {
            if (_service.CanProcess(request))
            {
                // actual logic buried here
            }
        }
    }
}

// ✅ GOOD - Guard clauses, flat structure
public Result Process(Request request)
{
    if (request == null) 
        return Result.Failure(EErrorType.ValidationError, "Request is null");
    
    if (!request.IsValid) 
        return Result.Failure(EErrorType.ValidationError, "Invalid request");
    
    if (!_service.CanProcess(request)) 
        return Result.Failure(EErrorType.ValidationError, "Cannot process");
    
    // actual logic at top level
}
```

---

## 📝 Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Private fields | Prefix with `_` | `_userData`, `_connectionString` |
| Constants | CAPITAL_LETTERS | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT` |
| Variables/Methods | camelCase | `getUserData()`, `isValid` |
| Interfaces | Prefix with `I` | `IUserService`, `IRepository` |
| Enums | Prefix with `E` | `EUserStatus`, `EConnectionState` |
| Functions | Short, descriptive names | `ValidateInput()`, `SendNotification()` |

---

## 🪵 Logging Standard

Use the `LogFlow` pattern for all logging:

```csharp
LogFlow{Level}.WriteLog("{MainFlow}", "{SubFlow}", "{Class}-{Function}-{Machine}-{Message}", operationId, exception);
```

**Levels:** `Info`, `Warning`, `Error`, `ErrorException`

**Concrete Example:**
```csharp
// Info log
LogFlowInfo.WriteLog("FileSync", "Upload", "FileManager-UploadAsync-SERVER01-Starting file upload for user", operationId);

// Error with exception
LogFlowErrorException.WriteLog("FileSync", "Upload", "FileManager-UploadAsync-SERVER01-Failed to upload file", operationId, ex);
```

---

## 🖥️ WPF Standards

### Architecture: MVVM (No External Libraries)

**Folder Structure:**
```
/ProjectName
├── /Views              # XAML files only
├── /ViewModels         # UI logic only (no business logic)
├── /Services
│   ├── /Managers       # Main flow orchestration
│   └── /SubServices    # Smaller operations (easy mocking)
├── /Models             # Data models
├── /Commands           # ICommand implementations
├── /Converters         # Value converters
├── /Resources          # Styles, templates, assets
└── /Helpers            # Utility classes
```

### Responsibilities:
- **View (XAML)** - UI layout and bindings only
- **ViewModel** - All and ONLY UI logic
- **Services/Managers** - Business logic, main flow orchestration
- **SubServices** - Smaller operations, designed for easy mocking

### Exception Handling:
- **UI should NOT handle exceptions** - Bubble up to services
- Services handle exceptions with Try-Catch-Finally pattern

---

## 🔧 Backend Standards (.NET)

### Architecture:
- **Always use Controllers** (not Minimal APIs)
- **Always use Dependency Injection**
- Target version: Specified per project

**Folder Structure:**
```
/ProjectName
├── /Controllers        # API endpoints
├── /Services           # Business logic
├── /Repositories       # Data access
├── /Models
│   ├── /Entities       # Database entities
│   ├── /DTOs           # Data transfer objects
│   └── /Requests       # API request models
├── /Interfaces         # Service contracts
├── /Middleware         # Custom middleware
├── /Extensions         # Extension methods
└── /Helpers            # Utilities
```

---

## ⚠️ Exception Handling

**Pattern:** Try-Catch-Finally

```csharp
public async Task<Result> ProcessDataAsync(string input, Guid operationId)
{
    try
    {
        LogFlowInfo.WriteLog("DataProcess", "Validation", "DataService-ProcessDataAsync-SERVER01-Starting process", operationId);
        
        // Business logic here
        
        return Result.Success();
    }
    catch (ValidationException ex)
    {
        LogFlowError.WriteLog("DataProcess", "Validation", "DataService-ProcessDataAsync-SERVER01-Validation failed", operationId, ex);
        throw;
    }
    catch (Exception ex)
    {
        LogFlowErrorException.WriteLog("DataProcess", "Validation", "DataService-ProcessDataAsync-SERVER01-Unexpected error", operationId, ex);
        throw;
    }
    finally
    {
        // Cleanup resources
    }
}
```

---

## 🧪 Unit Testing Standards

### Pattern: AAA (Arrange-Act-Assert)
### Library: FluentAssertions

```csharp
[Fact]
public async Task GetUser_WithValidId_ReturnsUser()
{
    // Arrange
    var userId = Guid.NewGuid();
    var expectedUser = new User { Id = userId, Name = "Test" };
    _mockUserRepository.Setup(x => x.GetByIdAsync(userId))
        .ReturnsAsync(expectedUser);

    // Act
    var result = await _userService.GetUserAsync(userId);

    // Assert
    result.Should().NotBeNull();
    result.Id.Should().Be(userId);
    result.Name.Should().Be("Test");
}
```

**Test Naming:** `{Method}_With{Condition}_Should{ExpectedResult}`

---

## 📦 Git Workflow

### On Task Completion, Provide:

1. **Commit Message** with full summary
2. **Impact Analysis** - What areas of the codebase are affected
3. **Suggested Tests** - What should be tested

**Example:**
```
feat: Add user authentication service

Summary:
- Implemented IAuthService with JWT token generation
- Added UserController endpoints for login/logout
- Created AuthMiddleware for token validation

Impact Analysis:
- New dependency: Microsoft.AspNetCore.Authentication.JwtBearer
- Affects: UserController, Startup.cs, appsettings.json
- Database: No changes

Suggested Tests:
- AuthService_Login_WithValidCredentials_ReturnsToken
- AuthService_Login_WithInvalidCredentials_ThrowsUnauthorized
- AuthMiddleware_WithExpiredToken_Returns401
- UserController_Logout_InvalidatesToken
```

---

## ⚛️ React Development Standards

### Mental Model: WPF → React Translation

| WPF Concept | React Equivalent | Notes |
|-------------|------------------|-------|
| UserControl | Component | Reusable UI piece |
| XAML | JSX/TSX | Declarative UI markup |
| Code-behind | Component function | Logic lives with UI |
| ViewModel | Hooks (useState, etc.) | State + UI logic |
| INotifyPropertyChanged | useState | Auto re-renders on change |
| ObservableCollection | useState with array | `setItems([...items, newItem])` |
| DependencyProperty | Props | Data passed parent → child |
| DataContext | Props / Context | How components get data |
| Binding | `{value}` in JSX | `{user.name}` instead of `{Binding Name}` |
| Commands (ICommand) | Event handlers | `onClick={handleClick}` |
| Styles/Resources | Tailwind classes / CSS | `className="bg-blue-500"` |
| DataTemplate | Component + map() | Render list items |
| Converter | Inline logic / util function | `{isActive ? "Yes" : "No"}` |
| Services (DI) | Custom hooks / Context | `useUserService()` |

### Core Concepts Explained Simply

**Components** = Functions that return UI
```tsx
// Like a UserControl - reusable, self-contained
function TaskCard({ task, onDelete }: TaskCardProps) {
  return (
    <div className="p-4 bg-white rounded shadow">
      <h3>{task.title}</h3>
      <button onClick={() => onDelete(task.id)}>Delete</button>
    </div>
  );
}
```

**Props** = Data flowing DOWN (parent → child)
```tsx
// Parent decides what data child receives (like DataContext)
<TaskCard task={myTask} onDelete={handleDelete} />
```

**State (useState)** = Data that changes and triggers re-render
```tsx
// Like INotifyPropertyChanged - change it, UI updates automatically
const [tasks, setTasks] = useState<Task[]>([]);
const [isLoading, setIsLoading] = useState(false);

// Update state (NEVER mutate directly!)
setTasks([...tasks, newTask]);  // ✅ Create new array
tasks.push(newTask);             // ❌ NEVER - won't trigger re-render
```

**useEffect** = React to changes / lifecycle
```tsx
// Like Loaded event + PropertyChanged combined
useEffect(() => {
  // Runs when component mounts (like Loaded)
  loadData();
  
  return () => {
    // Cleanup (like Unloaded/Dispose)
  };
}, []); // Empty array = run once on mount

useEffect(() => {
  // Runs whenever 'filter' changes (like PropertyChanged)
  filterTasks();
}, [filter]); // Dependency array - what to watch
```

### Folder Structure (TypeScript + Tailwind)

```
/src
├── /components          # Reusable UI components
│   ├── /common          # Buttons, Inputs, Modals (like base UserControls)
│   └── /features        # Feature-specific components
│       └── /kanban
│           ├── KanbanBoard.tsx
│           ├── KanbanColumn.tsx
│           └── TaskCard.tsx
├── /hooks               # Custom hooks (like Services in WPF)
│   ├── useLocalStorage.ts
│   └── useTasks.ts
├── /services            # API calls, business logic
│   └── taskService.ts
├── /types               # TypeScript interfaces/types
│   └── index.ts         # Export all types
├── /utils               # Helper functions
├── /context             # React Context (global state)
├── /pages               # Route pages (if using routing)
├── App.tsx              # Root component
└── main.tsx             # Entry point
```

### TypeScript Patterns

**Define Types First** (like your C# models)
```tsx
// /types/index.ts

export interface ITask {
  id: string;
  title: string;
  description: string;
  status: ETaskStatus;
  priority: ETaskPriority;
  createdAt: Date;
}

export enum ETaskStatus {
  TODO = "TODO",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE"
}

export enum ETaskPriority {
  LOW = "LOW",
  MEDIUM = "MEDIUM",
  HIGH = "HIGH"
}

// Props interface for components
export interface ITaskCardProps {
  task: ITask;
  onEdit: (task: ITask) => void;
  onDelete: (id: string) => void;
}
```

**Component with Props**
```tsx
// Explicit typing - like method signatures in C#
function TaskCard({ task, onEdit, onDelete }: ITaskCardProps) {
  return (/* JSX */);
}
```

### State Management Decision Tree

```
Is state used by ONE component only?
├── YES → useState (local state)
└── NO → Is it used by parent + few children?
    ├── YES → Lift state up + pass via props
    └── NO → Is it truly global (auth, theme, etc.)?
        ├── YES → React Context
        └── NO → Consider Zustand (simple) or Redux (complex)
```

**For Your Jira Clone:** Start with useState + props. Add Context only if prop drilling becomes painful (passing props through 4+ levels).

### Custom Hooks = Your Services

```tsx
// /hooks/useTasks.ts
// Like a TaskService in WPF - encapsulates all task logic

export function useTasks() {
  const [tasks, setTasks] = useState<ITask[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Load from localStorage (like your Repository)
  useEffect(() => {
    const saved = localStorage.getItem("tasks");
    if (saved) {
      setTasks(JSON.parse(saved));
    }
  }, []);

  // Save on change
  useEffect(() => {
    localStorage.setItem("tasks", JSON.stringify(tasks));
  }, [tasks]);

  const addTask = (task: Omit<ITask, "id" | "createdAt">) => {
    const newTask: ITask = {
      ...task,
      id: crypto.randomUUID(),
      createdAt: new Date()
    };
    setTasks(prev => [...prev, newTask]);
  };

  const updateTask = (id: string, updates: Partial<ITask>) => {
    setTasks(prev => prev.map(t => 
      t.id === id ? { ...t, ...updates } : t
    ));
  };

  const deleteTask = (id: string) => {
    setTasks(prev => prev.filter(t => t.id !== id));
  };

  const moveTask = (id: string, newStatus: ETaskStatus) => {
    updateTask(id, { status: newStatus });
  };

  return { tasks, isLoading, addTask, updateTask, deleteTask, moveTask };
}
```

**Usage in Component:**
```tsx
function KanbanBoard() {
  const { tasks, addTask, moveTask, deleteTask } = useTasks();
  
  // Now you have all task operations available
}
```

### Tailwind CSS Basics

```tsx
// Instead of WPF Styles, use utility classes directly
<div className="
  p-4              /* padding: 1rem (16px) */
  m-2              /* margin: 0.5rem */
  bg-white         /* background: white */
  rounded-lg       /* border-radius: large */
  shadow-md        /* box-shadow: medium */
  hover:bg-gray-50 /* hover state */
  flex             /* display: flex */
  items-center     /* align-items: center */
  justify-between  /* justify-content: space-between */
  gap-2            /* gap between flex items */
">

// Conditional classes (like Triggers in WPF)
<div className={`
  p-4 rounded
  ${isActive ? "bg-blue-500 text-white" : "bg-gray-100"}
  ${priority === "HIGH" ? "border-l-4 border-red-500" : ""}
`}>
```

### Common Patterns

**Rendering Lists** (like ItemsControl + DataTemplate)
```tsx
// WPF: ItemsSource + DataTemplate
// React: map() + component
{tasks.map(task => (
  <TaskCard 
    key={task.id}  // REQUIRED - unique identifier
    task={task}
    onDelete={deleteTask}
  />
))}
```

**Conditional Rendering** (like Visibility converters)
```tsx
// Show/hide
{isLoading && <Spinner />}

// If-else
{tasks.length > 0 ? (
  <TaskList tasks={tasks} />
) : (
  <EmptyState message="No tasks yet" />
)}
```

**Forms & Input** (like TextBox with Binding)
```tsx
const [title, setTitle] = useState("");

<input
  type="text"
  value={title}
  onChange={(e) => setTitle(e.target.value)}
  className="border rounded px-3 py-2"
  placeholder="Task title"
/>
```

### When Using .NET Backend

**Service Layer for API Calls:**
```tsx
// /services/taskService.ts
const API_BASE = "https://your-api.com/api";

export const taskService = {
  async getAll(): Promise<ITask[]> {
    const response = await fetch(`${API_BASE}/tasks`);
    if (!response.ok) throw new Error("Failed to fetch tasks");
    return response.json();
  },

  async create(task: Omit<ITask, "id">): Promise<ITask> {
    const response = await fetch(`${API_BASE}/tasks`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(task)
    });
    if (!response.ok) throw new Error("Failed to create task");
    return response.json();
  },

  // ... update, delete, etc.
};
```

**Hook Using Service:**
```tsx
export function useTasks() {
  const [tasks, setTasks] = useState<ITask[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadTasks = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await taskService.getAll();
      setTasks(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unknown error");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  return { tasks, isLoading, error, refetch: loadTasks };
}
```

### React Project Checklist

- [ ] Types/interfaces defined first (`/types`)
- [ ] Components are small, single-responsibility
- [ ] State lives at the lowest common parent
- [ ] Custom hooks for reusable logic
- [ ] No business logic in components (use hooks/services)
- [ ] Keys provided for all mapped lists
- [ ] Loading & error states handled
- [ ] TypeScript strict mode enabled

---

## 🔄 Post-Implementation Review

Before calling it done, verify:

- [ ] No dead code or unused variables
- [ ] Consistent naming throughout
- [ ] All null/empty checks in place
- [ ] No hardcoded values that should be configurable
- [ ] Opportunities to extract reusable abstractions identified
- [ ] No methods > 25 lines without justification
- [ ] No nesting > 3 levels

**The Litmus Test:** If I deleted this and rewrote it tomorrow, would I write it the same way?

---

## 🧩 When Stuck: Reframe

Don't force it. Instead:

1. **Break it down** - Split the problem into smaller subproblems
2. **Simplify first** - Solve the simplest version, then add complexity
3. **Identify the blocker** - What *specifically* makes this hard?
4. **Work backward** - Ask "What would make this trivial?" then reverse-engineer

**The Standard:** Code should feel inevitable—like there was no other way it could have been written.

---

## ✅ Pre-Code Checklist

- [ ] Studied existing codebase patterns and conventions
- [ ] Requirements are clear (asked questions if needed)
- [ ] Assumptions listed and challenged
- [ ] `<plan>` created with approaches and tradeoffs
- [ ] Task broken into small, verifiable steps
- [ ] Simplest solution identified (KISS)
- [ ] Logging added to all service methods
- [ ] Exception handling in place (Try-Catch-Finally)
- [ ] Unit tests written (AAA + FluentAssertions)
- [ ] Post-implementation review completed
- [ ] README updated
- [ ] Git commit message prepared with impact analysis
