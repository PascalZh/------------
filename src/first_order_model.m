nPop = 100;  % 种群大小
nCrossOver = round(nPop * 0.5);  % 交叉个体数
nMutation = round(nPop * 1);  % 变异个体数
global W D fixNode;

% 水质比例矩阵
W = csvread('../W.csv');
% 用水量向量
D = csvread('../D.csv');

fixNode = 1;  % 是否固定1和26节点为加氯点，这个节点为水源点

pop = zeros(nPop, 37);

optim_vars = 1;
while (optim_vars <= nPop)
    pop(optim_vars, :) = round(abs(rand(1, 37)-0.4));
    if fixNode
        pop(optim_vars, 1) = 1;
        pop(optim_vars, 26) = 1;
    end
    if (isValid(pop(optim_vars, :)))
        optim_vars = optim_vars + 1;
    end
end

for optim_vars = 1:5000
    popm = zeros(nMutation, 37);
    for j = 1:nMutation
        while (1)
            m = binaryMutate(pop(j, :));
            if (isValid(m))
                popm(j, :) = m;
                break;
            end
        end
    end

    popc = zeros(nCrossOver, 37);
    for j = 1:nCrossOver
        while (1)
            p1 = pop(round(rand * 35) + 1, :);
            p2 = pop(round(rand * 35) + 1, :);
            [c1, c2] = simpleXover(p1, p2);
            if (isValid(c1))
                popc(j, :) = c1;
                break;
            end
            if (isValid(c2))
                popc(j, :) = c2;
                break;
            end
        end
    end

    allPop = calculateFit([pop; popc; popm]);
    pop = roulette(allPop, nPop);
end

% 展示结果
pop_lw = zeros(1, 37);
pop_lw(7) = 1; pop_lw(22) = 1; pop_lw(35) = 1;
if fixNode
    pop_lw(1) = 1;
    pop_lw(26) = 1;
end
pop_lw = calculateFit(pop_lw);
fitness_lw = pop_lw(1, end);

disp('论文最优解：fitness_lw=');
disp(fitness_lw);
disp('论文最优解的用水量：sum Dx=');
disp(sum(max(D(find(pop_lw(1, 1:end-1))), 0)));
disp('论文最优解的加氯点：optim_vars=');
disp(find(pop_lw(1, 1:end-1)));

% find max fitness in the pop
maxidx = find(pop(:, end) == max(pop(:, end)));
optim_vars = find(pop(maxidx(1), 1:end-1));
fitness = pop(maxidx(1), end);
sum_Dx = sum(max(D(optim_vars), 0));
x = pop(maxidx(1), 1:end-1);
notj = find(1 - double(W * x' >= 1));
disp('最优解：fitness=');
disp(fitness);
disp('最优解的用水量：sum Dx=');
disp(sum_Dx);
disp('最优解的加氯点：optim_vars=');
disp(optim_vars);

function [newPop] = roulette(oldPop,numSols) 
    % 轮盘赌是典型的比例选择函数，其生存概率等于个体i的适应度与所有个体适应度之和的比值 

    % 产生相对的选择概率 
    minFit = 0;
    totalFit = sum(oldPop(:, end) - minFit + 1); 
    prob=(oldPop(:,end) - minFit + 1) / totalFit;  
    prob=cumsum(prob); 
      
    rNums=sort(rand(numSols,1));         % 产生随机数 
    % 从父代群体中选择个体遗传到子代 
    newPop = zeros(numSols, 37);
    fitIn=1;newIn=1;
    while newIn<=numSols 
        if(rNums(newIn)<prob(fitIn))
            newPop(newIn,:) = oldPop(fitIn,:); 
            newIn = newIn+1;
        else
            fitIn = fitIn + 1;
        end
    end 
end

function [c1,c2] = simpleXover(p1,p2) 
    % 从父代中选择两个个体P1,P2执行单点交叉 
     
    numVar = size(p1,2)-1;          % 获得变量个数  
    % 从1到变量个数间随机选择一个数作为单点交叉的位置 
    cPoint = round(rand * (numVar-2)) + 1; 
      
    c1 = [p1(1:cPoint) p2(cPoint+1:numVar+1)]; % 产生子代 
    c2 = [p2(1:cPoint) p1(cPoint+1:numVar+1)];

    global fixNode;
    if fixNode
        c1(1) = 1;
        c1(26) = 1;
        c2(1) = 1;
        c2(26) = 1;
    end
end

function [parent] = binaryMutate(parent) 
    % 基本位变异根据变异概率改变父代个体中的每一位 
     
    pm=0.02777; 
    numVar = size(parent,2)-1;      % 获得变量个数 
    % 从1到变量个数间随机选择一个数对该位进行变异 
    rN=rand(1,numVar)<pm; 
    parent=[abs(parent(1:numVar) - rN) parent(numVar+1)];
    
    global fixNode;
    if fixNode
        parent(1) = 1;
        parent(26) = 1;
    end
end 

function isValid = isValid(x)
    % 加氯点数不能超过3个（假如固定了1号点和26号点为加氯点，则不能超过5个）
    global fixNode
    if fixNode
        isValid = sum(x(1:36)) <= 5;
    else
        isValid = sum(x(1:36)) <= 3;
    end
end

function pop_with_fit = calculateFit(pop)
    global W D;
    pop_with_fit = pop;
    for i = 1:size(pop_with_fit, 1)
        x = pop(i, 1:end-1);
        y = double(W * x' >= 1);
        D_ = max(0, D);
        pop_with_fit(i, end) = D_ * y;
    end
end
